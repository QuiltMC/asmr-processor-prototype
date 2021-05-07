package org.quiltmc.asmr.processor;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.quiltmc.asmr.processor.annotation.AllowLambdaCapture;
import org.quiltmc.asmr.processor.annotation.HideFromTransformers;
import org.quiltmc.asmr.processor.capture.AsmrCapture;
import org.quiltmc.asmr.processor.capture.AsmrCopyNodeCaputre;
import org.quiltmc.asmr.processor.capture.AsmrCopySliceCapture;
import org.quiltmc.asmr.processor.capture.AsmrNodeCapture;
import org.quiltmc.asmr.processor.capture.AsmrReferenceCapture;
import org.quiltmc.asmr.processor.capture.AsmrReferenceNodeCapture;
import org.quiltmc.asmr.processor.capture.AsmrReferenceSliceCapture;
import org.quiltmc.asmr.processor.capture.AsmrSliceCapture;
import org.quiltmc.asmr.processor.tree.AsmrAbstractListNode;
import org.quiltmc.asmr.processor.tree.AsmrNode;
import org.quiltmc.asmr.processor.tree.AsmrTreeModificationManager;
import org.quiltmc.asmr.processor.tree.AsmrValueNode;
import org.quiltmc.asmr.processor.tree.asmvisitor.AsmrClassVisitor;
import org.quiltmc.asmr.processor.tree.member.AsmrClassNode;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.ref.SoftReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@AllowLambdaCapture
public class AsmrProcessor implements AutoCloseable {
    public static final int ASM_VERSION = Opcodes.ASM9;

    private static final Comparator<AsmrReferenceCapture> REF_CAPTURE_TREE_ORDER = (captureA, captureB) -> {
        int[] pathA = captureA.pathPrefix();
        int[] pathB = captureB.pathPrefix();
        int i;
        // find the point the two paths diverge, compare the children indexes at that point
        for (i = 0; i < pathA.length && i < pathB.length; i++) {
            if (pathA[i] != pathB[i]) {
                return Integer.compare(pathA[i], pathB[i]);
            }
        }

        if (pathA.length < pathB.length && captureA instanceof AsmrReferenceSliceCapture) {
            // pathA ended but captureA is a slice, so need to check the slice index
            int aStartIndex = ((AsmrReferenceSliceCapture<?, ?>) captureA).startIndexInclusive();
            return aStartIndex <= pathB[i] ? -1 : 1;
        } else if (pathB.length < pathA.length && captureB instanceof AsmrReferenceSliceCapture) {
            // pathB ended but captureB is a slice, so need to check the slice index
            int bStartIndex = ((AsmrReferenceSliceCapture<?, ?>) captureB).startIndexInclusive();
            return bStartIndex <= pathA[i] ? -1 : 1;
        } else if (pathA.length == pathB.length && captureA instanceof AsmrReferenceSliceCapture && captureB instanceof AsmrReferenceSliceCapture) {
            // pathA and pathB both end at the same time and are slices, compare their start indexes
            int aStartVirtualIndex = ((AsmrReferenceSliceCapture<?, ?>) captureA).startVirtualIndex();
            int bStartVirtualIndex = ((AsmrReferenceSliceCapture<?, ?>) captureB).startVirtualIndex();
            return Integer.compare(aStartVirtualIndex, bStartVirtualIndex);
        } else {
            // sort the outer one before the inner one
            return Integer.compare(pathA.length, pathB.length);
        }
    };

    private final AsmrPlatform platform;

    private final List<JarFile> jarFiles = new ArrayList<>();

    private final List<AsmrTransformer> transformers = new ArrayList<>();
    private final TreeMap<String, ClassProvider> allClasses = new TreeMap<>();
    private final TreeMap<String, String> config = new TreeMap<>();
    private List<String> phases = Arrays.asList(AsmrStandardPhases.READ_INITIAL, AsmrStandardPhases.READ_FINAL); // TODO: discuss a more comprehensive list

    private AsmrTransformerAction currentAction = null;
    private final ThreadLocal<String> currentWritingClassName = new ThreadLocal<>();
    private final ThreadLocal<Write> currentWrite = new ThreadLocal<>();
    private final Map<String, List<String>> roundDependents = new HashMap<>();
    private final Map<String, List<String>> writeDependents = new HashMap<>();
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<ClassRequest>> requestedClasses = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Write>> writes = new ConcurrentHashMap<>();
    private final Set<String> modifiedClasses = new HashSet<>();

    private final ConcurrentHashMap<String, ClassInfo> classInfoCache = new ConcurrentHashMap<>();

    private boolean upToDate = true;

    @HideFromTransformers
    public AsmrProcessor(AsmrPlatform platform) {
        this.platform = platform;
    }

    // ===== INPUTS ===== //

    // TODO: don't accept a class, accept bytecode and validate it
    @HideFromTransformers
    public void addTransformer(Class<? extends AsmrTransformer> transformerClass) {
        AsmrTransformer transformer;
        try {
            transformer = transformerClass.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        transformers.add(transformer);
    }

    /**
     * Adds all classes in the given jar to the processor. Invalidates the cache if the SHA-1 checksum of the jar
     * does not match the given checksum. Returns the SHA-1 checksum of the jar.
     */
    @HideFromTransformers
    public String addJar(Path jar, @Nullable String oldChecksum) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }

        try (ZipInputStream zip = new ZipInputStream(new DigestInputStream(Files.newInputStream(jar), digest))) {
            JarFile jarFile = new JarFile(jar.toFile());
            jarFiles.add(jarFile);

            ZipEntry zipEntry;
            while ((zipEntry = zip.getNextEntry()) != null) {
                String entryName = zipEntry.getName();
                if (entryName.endsWith(".class")) {
                    String className = entryName;
                    className = className.substring(0, className.length() - 6);
                    ClassProvider classProvider = new ClassProvider(() -> jarFile.getInputStream(jarFile.getJarEntry(entryName)));
                    allClasses.put(className, classProvider);
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Jar could not be read", e);
        }

        String checksum = Base64.getEncoder().encodeToString(digest.digest());
        if (!checksum.equals(oldChecksum)) {
            invalidateCache();
        }

        return checksum;
    }

    /**
     * Adds the class with the given internal name with the given bytecode to the processor. Cache is always invalidated
     * when calling this method.
     */
    @HideFromTransformers
    public void addClass(String className, byte[] bytecode) {
        invalidateCache();

        ClassProvider classProvider = new ClassProvider(() -> new ByteArrayInputStream(bytecode));
        allClasses.put(className, classProvider);
    }

    /**
     * Force-invalidates the cache.
     */
    @HideFromTransformers
    public void invalidateCache() {
        upToDate = false;
    }

    /**
     * Returns whether the cache is still valid.
     */
    public boolean isUpToDate() {
        return upToDate;
    }

    @HideFromTransformers
    public void addConfig(String key, String value) {
        config.put(key, value);
    }

    /**
     * Sets a custom list of phases.
     */
    @HideFromTransformers
    public void setPhases(List<String> phases) {
        this.phases = phases;
    }

    @HideFromTransformers
    @Override
    public void close() throws IOException {
        for (JarFile jarFile : jarFiles) {
            jarFile.close();
        }
    }

    // ===== PROCESSING ===== //

    @HideFromTransformers
    public void process() {
        if (upToDate) {
            return;
        }

        // compute rounds transformer action
        currentAction = AsmrTransformerAction.COMPUTE_ROUNDS;
        List<List<AsmrTransformer>> rounds = computeRounds();
        currentAction = null;

        for (List<AsmrTransformer> round : rounds) {
            runReadWriteRound(round);
        }

    }

    private List<List<AsmrTransformer>> computeRounds() {
        Map<String, Integer> phaseIndexes = new HashMap<>();
        List<List<AsmrTransformer>> phases = new ArrayList<>(this.phases.size());
        for (int i = 0; i < this.phases.size(); i++) {
            phaseIndexes.put(this.phases.get(i), i);
            phases.add(new ArrayList<>());
        }
        for (AsmrTransformer transformer : transformers) {
            transformer.addDependencies(this);

            List<String> desiredPhases = transformer.getPhases();
            boolean foundPhase = false;
            for (String desiredPhase : desiredPhases) {
                if (desiredPhase == null) {
                    foundPhase = true;
                    break;
                }
                Integer index = phaseIndexes.get(desiredPhase);
                if (index != null) {
                    phases.get(index).add(transformer);
                    foundPhase = true;
                    break;
                }
            }
            if (!foundPhase) {
                throw new IllegalStateException("Could not find desired transformer phase: " + desiredPhases);
            }
        }
        List<List<AsmrTransformer>> rounds = new ArrayList<>();
        for (List<AsmrTransformer> phase : phases) {
            rounds.addAll(computeRoundDependencies(phase));
        }
        return rounds;
    }

    private List<List<AsmrTransformer>> computeRoundDependencies(List<AsmrTransformer> transformers) {
        return splitByDependencyGraphDepth(transformers, roundDependents);
    }

    private static List<List<AsmrTransformer>> splitByDependencyGraphDepth(List<AsmrTransformer> transformers, Map<String, List<String>> dependentsGraph) {
        if (transformers.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Integer> inDegrees = new HashMap<>();
        dependentsGraph.forEach((parent, dependents) -> {
            for (String dependent : dependents) {
                inDegrees.merge(dependent, 1, Integer::sum);
            }
            inDegrees.putIfAbsent(parent, 0);
        });

        for (AsmrTransformer transformer : transformers) {
            String transformerId = transformer.getClass().getName();
            inDegrees.putIfAbsent(transformerId, 0);
        }

        Map<String, Integer> depths = new HashMap<>(inDegrees.size());
        Queue<String> queue = new LinkedList<>();
        inDegrees.forEach((id, inDegree) -> {
            if (inDegree == 0) {
                queue.add(id);
                depths.put(id, 0);
            }
        });

        int visited = 0;
        int maxDepth = 0;

        while (!queue.isEmpty()) {
            String transformerId = queue.remove();
            visited++;
            List<String> dependents = dependentsGraph.get(transformerId);
            if (dependents != null && !dependents.isEmpty()) {
                int nextDepth = depths.get(transformerId) + 1;
                maxDepth = Math.max(nextDepth, maxDepth);
                for (String dependent : dependents) {
                    int inDegree = inDegrees.get(dependent) - 1;
                    inDegrees.put(dependent, inDegree);
                    depths.merge(dependent, nextDepth, Math::max);
                    if (inDegree == 0) {
                        queue.add(dependent);
                    }
                }
            }
        }
        if (visited != inDegrees.size()) {
            // TODO: report which transformers have cyclic dependencies
            throw new IllegalStateException("Cyclic dependencies");
        }

        List<List<AsmrTransformer>> transformersByDepth = new ArrayList<>(maxDepth + 1);
        for (int i = 0; i <= maxDepth; i++) {
            transformersByDepth.add(new ArrayList<>());
        }
        for (AsmrTransformer transformer : transformers) {
            String transformerId = transformer.getClass().getName();
            transformersByDepth.get(depths.get(transformerId)).add(transformer);
        }
        transformersByDepth.removeIf(List::isEmpty);

        return transformersByDepth;
    }

    private void runReadWriteRound(List<AsmrTransformer> transformers) {
        // read transformer action
        currentAction = AsmrTransformerAction.READ;
        transformers.parallelStream().forEach(transformer -> {
            try {
                AsmrTreeModificationManager.disableModification();
                transformer.read(this);
            } finally {
                AsmrTreeModificationManager.enableModification();
            }
        });

        while (!this.requestedClasses.isEmpty()) {
            ConcurrentHashMap<String, ConcurrentLinkedQueue<ClassRequest>> requestedClasses = this.requestedClasses;
            this.requestedClasses = new ConcurrentHashMap<>();
            requestedClasses.entrySet().parallelStream().forEach(entry -> {
                String className = entry.getKey();
                List<ClassRequest> requests = new ArrayList<>(entry.getValue());
                processReadRequestsForClass(className, requests);
            });
        }

        // TODO: detect conflicts

        // write transformer action
        currentAction = AsmrTransformerAction.WRITE;
        writes.entrySet().parallelStream().forEach(entry -> {
            String className = entry.getKey();
            ConcurrentLinkedQueue<Write> writes = entry.getValue();
            processWritesForClass(className, writes);
        });

        modifiedClasses.addAll(writes.keySet());
        for (String className : writes.keySet()) {
            classInfoCache.remove(className);
        }

        writes.clear();

        currentAction = null;
    }

    private void processReadRequestsForClass(String className, List<ClassRequest> requests) {
        boolean careAboutConstantPool = true;
        for (ClassRequest request : requests) {
            if (request.constantPoolPredicate == null) {
                careAboutConstantPool = false;
                break;
            }
        }

        if (careAboutConstantPool) {
            AsmrConstantPool constantPool;
            try {
                constantPool = allClasses.get(className).getConstantPool();
            } catch (IOException e) {
                throw new UncheckedIOException("Error reading class, did it get deleted?", e);
            }
            if (constantPool != null) {
                boolean shouldReadClass = false;
                for (ClassRequest request : requests) {
                    if (request.constantPoolPredicate != null && request.constantPoolPredicate.test(constantPool)) {
                        shouldReadClass = true;
                        break;
                    }
                }
                if (!shouldReadClass) {
                    return;
                }
            }
        }

        AsmrClassNode classNode;
        try {
            classNode = allClasses.get(className).get();
        } catch (IOException e) {
            throw new UncheckedIOException("Error reading class, did it get deleted?", e);
        }
        try {
            AsmrTreeModificationManager.disableModification();
            for (ClassRequest request : requests) {
                request.callback.accept(classNode);
            }
        } finally {
            AsmrTreeModificationManager.enableModification();
        }
    }

    private void processWritesForClass(String className, ConcurrentLinkedQueue<Write> writes) {
        try {
            ClassProvider classProvider = allClasses.get(className);
            classProvider.modifiedClass = classProvider.get();
        } catch (IOException e) {
            throw new UncheckedIOException("Error reading class, did it get deleted?", e);
        }

        try {
            currentWritingClassName.set(className);

            List<Write> sortedWrites = sortWritesAndDetectConflicts(new ArrayList<>(writes));

            Set<AsmrReferenceCapture> allRefCapturesSet = new LinkedHashSet<>();
            for (Write write : sortedWrites) {
                allRefCapturesSet.add(write.target);
                for (AsmrCapture refCaptureInput : write.refCaptureInputs) {
                    allRefCapturesSet.add((AsmrReferenceCapture) refCaptureInput);
                }
            }
            List<AsmrReferenceCapture> sortedRefCaptures = new ArrayList<>(allRefCapturesSet);
            sortedRefCaptures.sort(REF_CAPTURE_TREE_ORDER);

            for (Write write : sortedWrites) {
                currentWrite.set(write);
                if (write.target instanceof AsmrNodeCapture) {
                    copyFrom(((AsmrNodeCapture<?>) write.target).resolved(this), write.replacementSupplier.get());
                } else {
                    AsmrSliceCapture<?> sliceCapture = (AsmrSliceCapture<?>) write.target;
                    AsmrAbstractListNode<?, ?> list = sliceCapture.resolvedList(this);
                    int startIndex = sliceCapture.startIndexInclusive();
                    int endIndex = sliceCapture.endIndexExclusive();
                    list.remove(startIndex, endIndex);
                    AsmrAbstractListNode<?, ?> replacement = (AsmrAbstractListNode<?, ?>) write.replacementSupplier.get();
                    insertCopy(list, startIndex, replacement);

                    shiftRefCapturesFrom(sortedRefCaptures, sliceCapture, endIndex - startIndex + replacement.size());
                }
            }
        } finally {
            currentWritingClassName.set(null);
            currentWrite.set(null);
        }
    }

    private void shiftRefCapturesFrom(List<AsmrReferenceCapture> sortedRefCaptures, AsmrSliceCapture<?> start, int shiftBy) {
        int startVirtualIndex = ((AsmrReferenceSliceCapture<?, ?>) start).startVirtualIndex();
        int index = Collections.binarySearch(sortedRefCaptures, (AsmrReferenceCapture) start, REF_CAPTURE_TREE_ORDER);
        assert index >= 0;

        int[] pathPrefixA = ((AsmrReferenceCapture) start).pathPrefix();
        backwardsLoop:
        for (int i = index; i >= 0; i--) {
            AsmrReferenceCapture refCapture = sortedRefCaptures.get(i);
            int[] pathPrefixB = refCapture.pathPrefix();

            // check if pathPrefixB starts with pathPrefixA
            if (pathPrefixB.length < pathPrefixA.length) {
                break backwardsLoop;
            }
            for (int j = pathPrefixA.length - 1; j >= 0; j--) {
                if (pathPrefixB[j] != pathPrefixA[j]) {
                    break backwardsLoop;
                }
            }

            if (refCapture instanceof AsmrReferenceSliceCapture) {
                AsmrReferenceSliceCapture<?, ?> slice = (AsmrReferenceSliceCapture<?, ?>) refCapture;
                if (slice.endVirtualIndex() > startVirtualIndex) {
                    slice.shiftEndVirtualIndex(shiftBy * 2);
                }
            }
        }

        forwardsLoop:
        for (int i = index + 1; i < sortedRefCaptures.size(); i++) {
            AsmrReferenceCapture refCapture = sortedRefCaptures.get(i);
            int[] pathPrefixB = refCapture.pathPrefix();

            // check if pathPrefixB starts with pathPrefixA
            if (pathPrefixB.length < pathPrefixA.length) {
                break forwardsLoop;
            }
            for (int j = pathPrefixA.length - 1; j >= 0; j--) {
                if (pathPrefixB[j] != pathPrefixA[j]) {
                    break forwardsLoop;
                }
            }

            if (pathPrefixB.length > pathPrefixA.length) {
                pathPrefixB[pathPrefixA.length] += shiftBy;
            } else if (refCapture instanceof AsmrReferenceSliceCapture) {
                AsmrReferenceSliceCapture<?, ?> slice = (AsmrReferenceSliceCapture<?, ?>) refCapture;
                if (slice.startVirtualIndex() > startVirtualIndex) {
                    slice.shiftStartVirtualIndex(shiftBy * 2);
                }
                if (slice.endVirtualIndex() > startVirtualIndex) {
                    slice.shiftEndVirtualIndex(shiftBy * 2);
                }
            }
        }
    }

    private List<Write> sortWritesAndDetectConflicts(List<Write> writes) {
        Map<String, List<Write>> writesByTransformer = writes.stream().collect(Collectors.groupingBy(write -> write.transformer.getClass().getName()));
        Map<Write, LinkedHashSet<Write>> hardDependents = new HashMap<>(this.writeDependents.size());
        Map<Write, LinkedHashSet<Write>> softDependents = new HashMap<>();

        // transfer transformer write dependents into hard write dependents
        this.writeDependents.forEach((transformerId, dependents) -> {
            List<Write> writesThisTransformer = writesByTransformer.get(transformerId);
            if (writesThisTransformer != null) {
                for (Write write : writesThisTransformer) {
                    LinkedHashSet<Write> dependentWrites = new LinkedHashSet<>();
                    hardDependents.put(write, dependentWrites);
                    for (String dependent : dependents) {
                        List<Write> dependentWritesToAdd = writesByTransformer.get(dependent);
                        if (dependentWritesToAdd != null) {
                            dependentWrites.addAll(dependentWritesToAdd);
                        }
                    }
                }
            }
        });

        class Ref {
            final AsmrReferenceCapture capture;
            final Write write;
            final boolean isInput;

            Ref(AsmrReferenceCapture capture, Write write, boolean isInput) {
                this.capture = capture;
                this.write = write;
                this.isInput = isInput;
            }
        }

        List<Ref> refs = new ArrayList<>();
        for (Write write : writes) {
            refs.add(new Ref(write.target, write, false));
            for (AsmrCapture input : write.refCaptureInputs) {
                refs.add(new Ref((AsmrReferenceCapture) input, write, true));
            }
        }

        refs.sort((refA, refB) -> REF_CAPTURE_TREE_ORDER.compare(refA.capture, refB.capture));

        for (int indexA = 0; indexA < refs.size(); indexA++) {
            Ref refA = refs.get(indexA);
            int[] pathPrefixA = refA.capture.pathPrefix();

            bLoop:
            for (int indexB = indexA + 1; indexB < refs.size(); indexB++) {
                Ref refB = refs.get(indexB);
                int[] pathPrefixB = refB.capture.pathPrefix();

                // check that the b prefix starts with the a prefix
                if (pathPrefixB.length < pathPrefixA.length) {
                    break bLoop;
                }
                for (int i = pathPrefixA.length - 1; i >= 0; i--) {
                    if (pathPrefixB[i] != pathPrefixA[i]) {
                        break bLoop;
                    }
                }

                // check b isn't past the end of a if a is a capture
                if (refA.capture instanceof AsmrReferenceSliceCapture) {
                    AsmrReferenceSliceCapture<?, ?> sliceA = (AsmrReferenceSliceCapture<?, ?>) refA.capture;
                    if (pathPrefixB.length > pathPrefixA.length) {
                        if (pathPrefixB[pathPrefixA.length] >= sliceA.endIndexExclusive()) {
                            break bLoop;
                        }
                    } else { // if (pathPrefixA.length == pathPrefixB.length)
                        if (refB.capture instanceof AsmrReferenceSliceCapture) {
                            AsmrReferenceSliceCapture<?, ?> sliceB = (AsmrReferenceSliceCapture<?, ?>) refB.capture;
                            if (sliceB.startVirtualIndex() >= sliceA.endVirtualIndex()) {
                                break bLoop;
                            }

                            // special case: check if b is an empty slice on the edge of a, then it's not colliding
                            if (sliceB.startVirtualIndex() == sliceB.endVirtualIndex()
                                    && (sliceA.startVirtualIndex() == sliceB.startVirtualIndex() || sliceA.endVirtualIndex() == sliceB.startVirtualIndex())) {
                                continue bLoop;
                            }
                        }
                    }
                }

                // at this point we know that refA collides with refB

                // check if they are from the same write for early exit
                if (refA.write == refB.write) {
                    continue bLoop;
                }

                // if they are both inputs they never impose a restriction
                if (refA.isInput && refB.isInput) {
                    continue bLoop;
                }

                // check if they are the same capture (no dependency restriction)
                if ((refA.capture instanceof AsmrReferenceSliceCapture) == (refB.capture instanceof AsmrReferenceSliceCapture)) {
                    if (pathPrefixA.length == pathPrefixB.length) {
                        if (refA.capture instanceof AsmrReferenceSliceCapture) {
                            int startA = ((AsmrReferenceSliceCapture<?, ?>) refA.capture).startVirtualIndex();
                            int endA = ((AsmrReferenceSliceCapture<?, ?>) refA.capture).endVirtualIndex();
                            int startB = ((AsmrReferenceSliceCapture<?, ?>) refB.capture).startVirtualIndex();
                            int endB = ((AsmrReferenceSliceCapture<?, ?>) refB.capture).endVirtualIndex();
                            if (startA == startB && endA == endB) {
                                continue bLoop;
                            }
                        } else {
                            continue bLoop;
                        }
                    }
                }

                // check if b is completely contained within a
                boolean bInsideA = false;

                // if a or b isn't a slice there is no other possible case then a complete containment
                if (!(refA.capture instanceof AsmrReferenceSliceCapture) || !(refB.capture instanceof AsmrReferenceSliceCapture)) {
                    bInsideA = true;
                } else if (pathPrefixA.length != pathPrefixB.length) {
                    bInsideA = true;
                } else {
                    int startA = ((AsmrReferenceSliceCapture<?, ?>) refA.capture).startVirtualIndex();
                    int endA = ((AsmrReferenceSliceCapture<?, ?>) refA.capture).endVirtualIndex();
                    int startB = ((AsmrReferenceSliceCapture<?, ?>) refB.capture).startVirtualIndex();
                    int endB = ((AsmrReferenceSliceCapture<?, ?>) refB.capture).endVirtualIndex();
                    if (startB >= startA && endB <= endA) {
                        bInsideA = true;
                    }
                }

                if (bInsideA) {
                    if (refA.isInput) {
                        softDependents.computeIfAbsent(refB.write, k -> new LinkedHashSet<>()).add(refA.write);
                    } else {
                        hardDependents.computeIfAbsent(refB.write, k -> new LinkedHashSet<>()).add(refA.write);
                    }
                    continue bLoop;
                }

                // at this point we know they are slices which overlap like this:
                // ^---^
                //   ^---^

                // if they are both not inputs, then they conflict - the cyclic dependencies encode this
                if (!refB.isInput) {
                    hardDependents.computeIfAbsent(refA.write, k -> new LinkedHashSet<>()).add(refB.write);
                }
                if (!refA.isInput) {
                    hardDependents.computeIfAbsent(refB.write, k -> new LinkedHashSet<>()).add(refA.write);
                }
            }
        }

        // promote soft dependencies to hard dependencies where they don't conflict the other way
        // TODO: this doesn't detect transitive hard dependencies
        softDependents.forEach((write, dependents) -> {
            for (Write dependent : dependents) {
                Set<Write> inverseHardDependents = hardDependents.get(dependent);
                if (inverseHardDependents == null || !inverseHardDependents.contains(write)) {
                    hardDependents.computeIfAbsent(write, k -> new LinkedHashSet<>()).add(dependent);
                }
            }
        });

        // topological sort the writes based on dependencies
        Map<Write, Integer> inDegrees = new HashMap<>();
        for (Write write : writes) {
            inDegrees.put(write, 0);
        }
        for (Set<Write> dependents : hardDependents.values()) {
            for (Write dependent : dependents) {
                inDegrees.merge(dependent, 1, Integer::sum);
            }
        }

        Queue<Write> writesToVisit = new LinkedList<>();
        inDegrees.forEach((write, degrees) -> {
            if (degrees == 0) {
                writesToVisit.add(write);
            }
        });

        List<Write> sortedWrites = new ArrayList<>(writes.size());

        while (!writesToVisit.isEmpty()) {
            Write write = writesToVisit.remove();
            sortedWrites.add(write);
            Set<Write> dependents = hardDependents.get(write);
            if (dependents != null) {
                for (Write dependent : dependents) {
                    int newInDegree = inDegrees.get(dependent) - 1;
                    inDegrees.put(dependent, newInDegree);
                    if (newInDegree == 0) {
                        writesToVisit.add(dependent);
                    }
                }
            }
        }

        if (sortedWrites.size() != writes.size()) {
            // TODO: descriptive error message
            throw new IllegalStateException("Cyclic explicit or implicit write dependencies");
        }

        return sortedWrites;
    }

    @SuppressWarnings("unchecked")
    private static <T extends AsmrNode<T>> void copyFrom(AsmrNode<?> into, AsmrNode<?> from) {
        ((T) into).copyFrom((T) from);
    }

    @SuppressWarnings("unchecked")
    private static <T extends AsmrNode<T>> void insertCopy(AsmrAbstractListNode<T, ?> into, int index, AsmrAbstractListNode<?, ?> from) {
        into.insertCopy(index, (AsmrAbstractListNode<? extends T, ?>) from);
    }

    @HideFromTransformers
    @Nullable
    public AsmrClassNode findClassImmediately(String name) {
        ClassProvider classProvider = allClasses.get(name);
        if (classProvider == null) {
            return null;
        }
        try {
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (classProvider) {
                return classProvider.get();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Error reading class, did it get deleted?", e);
        }
    }

    @HideFromTransformers
    public Collection<String> getModifiedClassNames() {
        return modifiedClasses;
    }

    // ===== TRANSFORMER METHODS ===== //

    /**
     * Causes the current transformer to run in a round after the other transformer
     */
    public void addRoundDependency(AsmrTransformer self, String otherTransformerId) {
        checkAction(AsmrTransformerAction.COMPUTE_ROUNDS);
        roundDependents.computeIfAbsent(otherTransformerId, k -> new ArrayList<>()).add(self.getClass().getName());
    }

    /**
     * Causes the current transformer to run in a round before the other transformer
     */
    public void addRoundDependent(AsmrTransformer self, String otherTransformerId) {
        checkAction(AsmrTransformerAction.COMPUTE_ROUNDS);
        roundDependents.computeIfAbsent(self.getClass().getName(), k -> new ArrayList<>()).add(otherTransformerId);
    }

    /**
     * Causes the current transformer to write as if after the other transformer
     */
    public void addWriteDependency(AsmrTransformer self, String otherTransformerId) {
        checkAction(AsmrTransformerAction.COMPUTE_ROUNDS);
        writeDependents.computeIfAbsent(otherTransformerId, k -> new ArrayList<>()).add(self.getClass().getName());
    }

    /**
     * Causes the current transformer to write as if before the other transformer
     */
    public void addWriteDependent(AsmrTransformer self, String otherTransformerId) {
        checkAction(AsmrTransformerAction.COMPUTE_ROUNDS);
        writeDependents.computeIfAbsent(self.getClass().getName(), k -> new ArrayList<>()).add(otherTransformerId);
    }

    public boolean classExists(String name) {
        return allClasses.containsKey(name);
    }

    public void withClass(String name, Consumer<? super AsmrClassNode> callback) {
        checkAction(AsmrTransformerAction.READ);
        if (!allClasses.containsKey(name)) {
            throw new IllegalArgumentException("Class not found: " + name);
        }
        requestedClasses.computeIfAbsent(name, k -> new ConcurrentLinkedQueue<>()).add(new ClassRequest(null, callback));
    }

    /**
     * Performs a read action on all classes matching a name predicate and a predicate on the class' constant pool.
     * Note that the constant pool filter is for optimization purposes only, and the processor is free in some
     * situations to call the callback on classes which don't match the constant pool even if the constant pool filter
     * returns false on that class. This is to preserve pureness of the transformation process, and to take into account
     * that the constant pool can become outdated once the class has been written to.
     */
    @ApiStatus.Experimental // we don't know whether predicating on the constant pool is worth it yet
    public void withClasses(Predicate<? super String> namePredicate, @Nullable Predicate<? super AsmrConstantPool> constantPoolPredicate, Consumer<? super AsmrClassNode> callback) {
        checkAction(AsmrTransformerAction.READ);
        for (String className : allClasses.keySet()) {
            if (namePredicate.test(className)) {
                requestedClasses.computeIfAbsent(className, k -> new ConcurrentLinkedQueue<>()).add(new ClassRequest(constantPoolPredicate, callback));
            }
        }
    }

    public void withClasses(Predicate<? super String> namePredicate, Consumer<? super AsmrClassNode> callback) {
        withClasses(namePredicate, null, callback);
    }

    public void withClasses(String prefix, Consumer<? super AsmrClassNode> callback) {
        withClasses(name -> name.startsWith(prefix), callback);
    }

    public void withAllClasses(Consumer<? super AsmrClassNode> callback) {
        withClasses(name -> true, callback);
    }

    public <T extends AsmrNode<T>> AsmrNodeCapture<T> copyCapture(T node) {
        checkAction(AsmrTransformerAction.READ);
        try {
            AsmrTreeModificationManager.enableModification();
            return new AsmrCopyNodeCaputre<>(node);
        } finally {
            AsmrTreeModificationManager.disableModification();
        }
    }

    public <T extends AsmrNode<T>> AsmrNodeCapture<T> refCapture(T node) {
        checkAction(AsmrTransformerAction.READ);
        return new AsmrReferenceNodeCapture<>(node);
    }

    public <T extends AsmrNode<T>> AsmrSliceCapture<T> copyCapture(AsmrAbstractListNode<T, ?> list, int startInclusive, int endExclusive) {
        checkAction(AsmrTransformerAction.READ);
        if (startInclusive < 0 || endExclusive < startInclusive || endExclusive > list.size()) {
            throw new IndexOutOfBoundsException(String.format("[%d, %d), size %d", startInclusive, endExclusive, list.size()));
        }
        try {
            AsmrTreeModificationManager.enableModification();
            return new AsmrCopySliceCapture<>(list, startInclusive, endExclusive);
        } finally {
            AsmrTreeModificationManager.disableModification();
        }
    }

    /**
     * Creates a ref slice capture between {@code startVirtualIndex} and {@code endVirtualIndex} in the given list.
     * The "virtual index" in a list includes the meaning of index and whether you want to point before/after a certain
     * node. There are two virtual indexes per "gap" in the list, starting at index 0 and ending with
     * {@code list.size() * 2 + 1}. To find the virtual index before node {@code i}, use {@code i * 2 + 1}, to find the
     * virtual index after node {@code i}, use {@code i * 2 + 2}.
     *
     * @param list The list to capture in
     * @param startVirtualIndex The start virtual index (inclusive)
     * @param endVirtualIndex The end virtual index (exclusive)
     */
    public <T extends AsmrNode<T>> AsmrSliceCapture<T> refCapture(AsmrAbstractListNode<T, ?> list, int startVirtualIndex, int endVirtualIndex) {
        checkAction(AsmrTransformerAction.READ);
        if (startVirtualIndex < 0 || endVirtualIndex >= list.size() * 2 + 2 || endVirtualIndex < startVirtualIndex) {
            throw new IllegalArgumentException(String.format("Virtual [%d, %d), size %d", startVirtualIndex, endVirtualIndex, list.size()));
        }
        return new AsmrReferenceSliceCapture<>(list, startVirtualIndex, endVirtualIndex);
    }

    public <T extends AsmrNode<T>> void addWrite(
            AsmrTransformer transformer,
            AsmrNodeCapture<T> target,
            Supplier<? extends T> replacementSupplier
    ) {
        addWrite(transformer, target, replacementSupplier, Collections.emptySet());
    }

    public <T extends AsmrNode<T>> void addWrite(
            AsmrTransformer transformer,
            AsmrNodeCapture<T> target,
            Supplier<? extends T> replacementSupplier,
            Set<AsmrCapture> refCaptureInputs
    ) {
        checkAction(AsmrTransformerAction.READ);
        if (transformer == null) {
            throw new NullPointerException();
        }
        if (!(target instanceof AsmrReferenceCapture)) {
            throw new IllegalArgumentException("Target must be a reference capture, not a copy capture");
        }
        AsmrReferenceCapture refTarget = (AsmrReferenceCapture) target;
        Write write = new Write(transformer, refTarget, replacementSupplier, refCaptureInputs);
        writes.computeIfAbsent(refTarget.className(), k -> new ConcurrentLinkedQueue<>()).add(write);
    }

    public <T extends AsmrNode<T>, L extends AsmrAbstractListNode<T, L>> void addWrite(
            AsmrTransformer transformer,
            AsmrSliceCapture<T> target,
            Supplier<? extends L> replacementSupplier
    ) {
        addWrite(transformer, target, replacementSupplier, Collections.emptySet());
    }

    public <T extends AsmrNode<T>, L extends AsmrAbstractListNode<T, L>> void addWrite(
            AsmrTransformer transformer,
            AsmrSliceCapture<T> target,
            Supplier<? extends L> replacementSupplier,
            Set<AsmrCapture> refCaptureInputs
    ) {
        checkAction(AsmrTransformerAction.READ);
        if (transformer == null) {
            throw new NullPointerException();
        }
        if (!(target instanceof AsmrReferenceCapture)) {
            throw new IllegalArgumentException("Target must be a reference capture, not a copy capture");
        }
        AsmrReferenceCapture refTarget = (AsmrReferenceCapture) target;
        Write write = new Write(transformer, refTarget, replacementSupplier, refCaptureInputs);
        writes.computeIfAbsent(refTarget.className(), k -> new ConcurrentLinkedQueue<>()).add(write);
    }

    public <T extends AsmrNode<T>> void substitute(T target, AsmrNodeCapture<T> source) {
        checkAction(AsmrTransformerAction.WRITE);
        if (source instanceof AsmrReferenceCapture) {
            if (!currentWrite.get().refCaptureInputs.contains(source)) {
                throw new IllegalArgumentException("Cannot substitute a ref capture which has not been declared as an input to the current write");
            }
        }

        target.copyFrom(source.resolved(this));
    }

    @SuppressWarnings("unchecked")
    public <E extends AsmrNode<E>, L extends AsmrAbstractListNode<E, L>> void substitute(L target, int index, AsmrSliceCapture<E> source) {
        checkAction(AsmrTransformerAction.WRITE);
        if (source instanceof AsmrReferenceCapture) {
            if (!currentWrite.get().refCaptureInputs.contains(source)) {
                throw new IllegalArgumentException("Cannot substitute a ref capture which has not been declared as an input to the current write");
            }
        }

        L resolvedList = (L) source.resolvedList(this);
        int startIndex = source.startIndexInclusive();
        int endIndex = source.endIndexExclusive();
        for (int i = startIndex; i < endIndex; i++) {
            target.insertCopy(index + i, resolvedList.get(i));
        }
    }

    @Nullable
    public String getConfigValue(String key) {
        return config.get(key);
    }

    public void log(String message) {
        System.out.println(message);
    }

    // ===== PRIVATE UTILITIES ===== //

    private ClassInfo getClassInfo(String type) {
        return classInfoCache.computeIfAbsent(type, type1 -> {
            ClassProvider classProvider = allClasses.get(type1);
            if (classProvider != null && classProvider.modifiedClass != null) {
                boolean isInterface = false;
                for (AsmrValueNode<Integer> modifier : classProvider.modifiedClass.modifiers()) {
                    if (modifier.value() == Opcodes.ACC_INTERFACE) {
                        isInterface = true;
                    }
                }
                return new ClassInfo(classProvider.modifiedClass.superclass().value(), isInterface);
            }

            byte[] bytecode;
            try {
                bytecode = platform.getClassBytecode(type1);
            } catch (ClassNotFoundException e) {
                throw new TypeNotPresentException(type1, e);
            }

            ClassReader reader = new ClassReader(bytecode);

            class ClassInfoVisitor extends ClassVisitor {
                String superName;
                boolean isInterface;

                ClassInfoVisitor() {
                    super(ASM_VERSION);
                }

                @Override
                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                    this.superName = superName;
                    this.isInterface = (access & Opcodes.ACC_INTERFACE) != 0;
                }
            }

            ClassInfoVisitor cv = new ClassInfoVisitor();
            reader.accept(cv, ClassReader.SKIP_CODE);
            return new ClassInfo(cv.superName, cv.isInterface);
        });
    }

    private boolean isDerivedFrom(String subtype, String supertype) {
        subtype = getClassInfo(subtype).superClass;

        Set<String> visitedTypes = new HashSet<>();

        while (subtype != null) {
            if (!visitedTypes.add(subtype)) {
                return false;
            }
            if (supertype.equals(subtype)) {
                return true;
            }
            subtype = getClassInfo(subtype).superClass;
        }

        return false;
    }

    @ApiStatus.Internal
    public String getCommonSuperClass(String type1, String type2) {
        if (type1 == null || type2 == null) {
            return "java/lang/Object";
        }

        if (isDerivedFrom(type1, type2)) {
            return type2;
        } else if (isDerivedFrom(type2, type1)) {
            return type1;
        } else if (getClassInfo(type1).isInterface || getClassInfo(type2).isInterface) {
            return "java/lang/Object";
        }

        do {
            type1 = getClassInfo(type1).superClass;
            if (type1 == null) {
                return "java/lang/Object";
            }
        } while (!isDerivedFrom(type2, type1));

        return type1;
    }

    @ApiStatus.Internal
    public void checkAction(AsmrTransformerAction expectedAction) {
        if (currentAction != expectedAction) {
            throw new IllegalStateException("This operation is only allowed in a " + expectedAction + " transformer action");
        }
    }

    @ApiStatus.Internal
    public void checkWritingClass(String className) {
        if (!className.equals(currentWritingClassName.get())) {
            throw new IllegalStateException("This operation is only allowed while writing class '" + className + "' but was writing '" + currentWritingClassName.get() + "'");
        }
    }

    private static class ClassProvider {
        private SoftReference<AsmrConstantPool> cachedConstantPool = null;
        private SoftReference<AsmrClassNode> cachedClass = null;
        public AsmrClassNode modifiedClass = null;
        private final InputStreamSupplier inputStreamSupplier;

        public ClassProvider(InputStreamSupplier inputStreamSupplier) {
            this.inputStreamSupplier = inputStreamSupplier;
        }

        /** Warning: NOT thread safe! */
        public AsmrClassNode get() throws IOException {
            if (modifiedClass != null) {
                return modifiedClass;
            }

            if (cachedClass != null) {
                AsmrClassNode val = cachedClass.get();
                if (val != null) {
                    return val;
                }
            }
            InputStream inputStream = inputStreamSupplier.get();
            ClassReader classReader = new ClassReader(inputStream);
            AsmrClassNode val = new AsmrClassNode();
            boolean wasModificationEnabled = AsmrTreeModificationManager.isModificationEnabled();
            try {
                AsmrTreeModificationManager.enableModification();
                classReader.accept(new AsmrClassVisitor(val), ClassReader.SKIP_FRAMES);
            } finally {
                if (!wasModificationEnabled) {
                    AsmrTreeModificationManager.disableModification();
                }
            }
            cachedClass = new SoftReference<>(val);
            return val;
        }

        /** Warning: NOT thread safe! */
        @Nullable
        public AsmrConstantPool getConstantPool() throws IOException {
            if (modifiedClass != null) {
                // class having been modified means the constant pool may have changed
                return null;
            }

            if (cachedConstantPool != null) {
                AsmrConstantPool constantPool = cachedConstantPool.get();
                if (constantPool != null) {
                    return constantPool;
                }
            }

            InputStream inputStream = new BufferedInputStream(inputStreamSupplier.get());
            AsmrConstantPool constantPool = AsmrConstantPool.read(inputStream);
            cachedConstantPool = new SoftReference<>(constantPool);
            return constantPool;
        }
    }

    @FunctionalInterface
    private interface InputStreamSupplier {
        InputStream get() throws IOException;
    }

    private static class ClassRequest {
        @Nullable
        public final Predicate<? super AsmrConstantPool> constantPoolPredicate;
        public final Consumer<? super AsmrClassNode> callback;

        public ClassRequest(@Nullable Predicate<? super AsmrConstantPool> constantPoolPredicate, Consumer<? super AsmrClassNode> callback) {
            this.constantPoolPredicate = constantPoolPredicate;
            this.callback = callback;
        }
    }

    private static class Write {
        public final AsmrTransformer transformer;
        public final AsmrReferenceCapture target;
        public final Set<AsmrCapture> refCaptureInputs;
        public final Supplier<? extends AsmrNode<?>> replacementSupplier;

        public Write(AsmrTransformer transformer, AsmrReferenceCapture target, Supplier<? extends AsmrNode<?>> replacementSupplier, Set<AsmrCapture> refCaptureInputs) {
            this.transformer = transformer;
            this.target = target;
            this.replacementSupplier = replacementSupplier;
            this.refCaptureInputs = refCaptureInputs;
        }
    }

    private static class ClassInfo {
        public final String superClass;
        public final boolean isInterface;

        public ClassInfo(String superClass, boolean isInterface) {
            this.superClass = superClass;
            this.isInterface = isInterface;
        }
    }
}
