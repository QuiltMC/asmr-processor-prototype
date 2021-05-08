package org.quiltmc.asmr.processor;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.asmr.processor.capture.AsmrCapture;
import org.quiltmc.asmr.processor.capture.AsmrNodeCapture;
import org.quiltmc.asmr.processor.capture.AsmrReferenceCapture;
import org.quiltmc.asmr.processor.capture.AsmrReferenceSliceCapture;
import org.quiltmc.asmr.processor.capture.AsmrSliceCapture;
import org.quiltmc.asmr.tree.AsmrAbstractListNode;
import org.quiltmc.asmr.tree.AsmrNode;
import org.quiltmc.asmr.tree.AsmrTreeModificationManager;
import org.quiltmc.asmr.tree.member.AsmrClassNode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

final class AsmrProcessorRunner {
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

    private final AsmrProcessor processor;

    private ConcurrentHashMap<String, ConcurrentLinkedQueue<ClassRequest>> requestedClasses = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Write>> writes = new ConcurrentHashMap<>();
    private final ThreadLocal<String> currentWritingClassName = new ThreadLocal<>();
    private final ThreadLocal<Write> currentWrite = new ThreadLocal<>();
    private AsmrProcessorAction currentAction = null;

    AsmrProcessorRunner(AsmrProcessor processor) {
        this.processor = processor;
    }

    void addClassRequest(String name, @Nullable Predicate<? super AsmrConstantPool> constantPoolPredicate, Consumer<? super AsmrClassNode> callback) {
        requestedClasses.computeIfAbsent(name, k -> new ConcurrentLinkedQueue<>()).add(new ClassRequest(constantPoolPredicate, callback));
    }

    <T extends AsmrNode<T>> void addWrite(AsmrTransformer transformer, AsmrReferenceCapture target, Supplier<? extends T> replacementSupplier, Set<AsmrCapture> refCaptureInputs) {
        writes.computeIfAbsent(target.className(), k -> new ConcurrentLinkedQueue<>()).add(new Write(transformer, target, replacementSupplier, refCaptureInputs));
    }

    void checkWritingClass(String className) {
        if (!className.equals(currentWritingClassName.get())) {
            throw new IllegalStateException("This operation is only allowed while writing class '" + className + "' but was writing '" + currentWritingClassName.get() + "'");
        }
    }

    void checkRefCaptureInput(AsmrCapture source) {
        if (source instanceof AsmrReferenceCapture) {
            if (!currentWrite.get().refCaptureInputs.contains(source)) {
                throw new IllegalArgumentException("Cannot substitute a ref capture which has not been declared as an input to the current write");
            }
        }
    }

    void checkAction(AsmrProcessorAction expectedAction) {
        if (currentAction != expectedAction) {
            throw new IllegalStateException("This operation is only allowed in a " + expectedAction + " transformer action");
        }
    }

    void process() {
        // compute rounds processor action
        currentAction = AsmrProcessorAction.COMPUTE_ROUNDS;
        List<List<AsmrTransformer>> rounds = computeRounds();
        currentAction = null;

        for (List<AsmrTransformer> round : rounds) {
            runReadWriteRound(round);
        }

    }

    private List<List<AsmrTransformer>> computeRounds() {
        Map<String, Integer> phaseIndexes = new HashMap<>();
        List<List<AsmrTransformer>> phases = new ArrayList<>(processor.phases.size());
        for (int i = 0; i < processor.phases.size(); i++) {
            phaseIndexes.put(processor.phases.get(i), i);
            phases.add(new ArrayList<>());
        }
        for (AsmrTransformer transformer : processor.transformers()) {
            transformer.addDependencies(processor);

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
        return splitByDependencyGraphDepth(transformers, processor.roundDependents());
    }

    private static List<List<AsmrTransformer>> splitByDependencyGraphDepth(List<AsmrTransformer> transformers, Map<String, List<String>> dependentsGraph) {
        if (transformers.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Integer> inDegrees = new LinkedHashMap<>();
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
        // read processor action
        currentAction = AsmrProcessorAction.READ;
        transformers.parallelStream().forEach(transformer -> {
            try {
                AsmrTreeModificationManager.disableModification();
                transformer.read(processor);
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

        // write processor action
        currentAction = AsmrProcessorAction.WRITE;
        writes.entrySet().parallelStream().forEach(entry -> {
            String className = entry.getKey();
            ConcurrentLinkedQueue<Write> writes = entry.getValue();
            processWritesForClass(className, writes);
        });

        processor.modifiedClasses().addAll(writes.keySet());
        for (String className : writes.keySet()) {
            processor.classInfoCache().remove(className);
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
                constantPool = processor.allClasses().get(className).getConstantPool();
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
            classNode = processor.allClasses().get(className).get();
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
            AsmrProcessor.ClassProvider classProvider = processor.allClasses().get(className);
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
                    copyFrom(((AsmrNodeCapture<?>) write.target).resolved(processor), write.replacementSupplier.get());
                } else {
                    AsmrSliceCapture<?> sliceCapture = (AsmrSliceCapture<?>) write.target;
                    AsmrAbstractListNode<?, ?> list = sliceCapture.resolvedList(processor);
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
        Map<Write, LinkedHashSet<Write>> hardDependents = new HashMap<>(processor.writeDependents().size());
        Map<Write, LinkedHashSet<Write>> softDependents = new HashMap<>();

        // transfer transformer write dependents into hard write dependents
        processor.writeDependents().forEach((transformerId, dependents) -> {
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
        softDependents.forEach((write, dependents) -> {
            Set<Write> visited = new HashSet<>();
            for (Write dependent : dependents) {
                // transitive dependency depth is expected to be shallow, so a simple recursive solution suffices for now
                if (isTransitivelyDependent(dependent, write, hardDependents, visited)) {
                    hardDependents.computeIfAbsent(write, k -> new LinkedHashSet<>()).add(dependent);
                }
            }
        });

        // topological sort the writes based on dependencies
        Map<Write, Integer> inDegrees = new LinkedHashMap<>();
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

    private static boolean isTransitivelyDependent(Write from, Write to, Map<Write, LinkedHashSet<Write>> dependents, Set<Write> visited) {
        LinkedHashSet<Write> depOnFrom = dependents.get(from);
        if (depOnFrom == null) {
            return false;
        }
        if (depOnFrom.contains(to)) {
            return true;
        }
        for (Write intermediate : depOnFrom) {
            if (!visited.add(to)) {
                return false; // cycle, will be detected later
            }
            if (isTransitivelyDependent(intermediate, to, dependents, visited)) {
                return true;
            }
            visited.remove(to);
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    private static <T extends AsmrNode<T>> void copyFrom(AsmrNode<?> into, AsmrNode<?> from) {
        ((T) into).copyFrom((T) from);
    }

    @SuppressWarnings("unchecked")
    private static <T extends AsmrNode<T>> void insertCopy(AsmrAbstractListNode<T, ?> into, int index, AsmrAbstractListNode<?, ?> from) {
        into.insertCopy(index, (AsmrAbstractListNode<? extends T, ?>) from);
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
}
