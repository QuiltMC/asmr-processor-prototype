package org.quiltmc.asmr.processor;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
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
import org.quiltmc.asmr.tree.AsmrAbstractListNode;
import org.quiltmc.asmr.tree.AsmrNode;
import org.quiltmc.asmr.tree.AsmrTreeModificationManager;
import org.quiltmc.asmr.tree.AsmrValueNode;
import org.quiltmc.asmr.tree.asmvisitor.AsmrClassVisitor;
import org.quiltmc.asmr.tree.member.AsmrClassNode;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@AllowLambdaCapture
public final class AsmrProcessor implements AutoCloseable {
    public static final int ASM_VERSION = Opcodes.ASM9;

    private final AsmrPlatform platform;

    private final List<JarFile> jarFiles = new ArrayList<>();

    private final List<AsmrTransformer> transformers = new ArrayList<>();
    private final Map<String, ClassProvider> allClasses = new HashMap<>();
    private final TreeMap<String, String> config = new TreeMap<>();
    private List<String> phases = Arrays.asList(AsmrStandardPhases.READ_INITIAL, AsmrStandardPhases.READ_FINAL); // TODO: discuss a more comprehensive list

    private final AsmrProcessorRunner processorRunner = new AsmrProcessorRunner(this);
    private final Map<String, List<String>> roundDependents = new HashMap<>();
    private final Map<String, List<String>> writeDependents = new HashMap<>();
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

        processorRunner.process();
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
            throw new UncheckedIOException("Error reading class, did it get deleted on disk?", e);
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
        processorRunner.checkAction(AsmrProcessorAction.COMPUTE_ROUNDS);
        roundDependents.computeIfAbsent(otherTransformerId, k -> new ArrayList<>()).add(self.getClass().getName());
    }

    /**
     * Causes the current transformer to run in a round before the other transformer
     */
    public void addRoundDependent(AsmrTransformer self, String otherTransformerId) {
        processorRunner.checkAction(AsmrProcessorAction.COMPUTE_ROUNDS);
        roundDependents.computeIfAbsent(self.getClass().getName(), k -> new ArrayList<>()).add(otherTransformerId);
    }

    /**
     * Causes the current transformer to write as if after the other transformer
     */
    public void addWriteDependency(AsmrTransformer self, String otherTransformerId) {
        processorRunner.checkAction(AsmrProcessorAction.COMPUTE_ROUNDS);
        writeDependents.computeIfAbsent(otherTransformerId, k -> new ArrayList<>()).add(self.getClass().getName());
    }

    /**
     * Causes the current transformer to write as if before the other transformer
     */
    public void addWriteDependent(AsmrTransformer self, String otherTransformerId) {
        processorRunner.checkAction(AsmrProcessorAction.COMPUTE_ROUNDS);
        writeDependents.computeIfAbsent(self.getClass().getName(), k -> new ArrayList<>()).add(otherTransformerId);
    }

    public boolean classExists(String name) {
        return allClasses.containsKey(name);
    }

    public void withClass(String name, Consumer<? super AsmrClassNode> callback) {
        processorRunner.checkAction(AsmrProcessorAction.READ);
        if (!allClasses.containsKey(name)) {
            throw new IllegalArgumentException("Class not found: " + name);
        }
        processorRunner.addClassRequest(name, null, callback);
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
        processorRunner.checkAction(AsmrProcessorAction.READ);
        for (String className : allClasses.keySet()) {
            if (namePredicate.test(className)) {
                processorRunner.addClassRequest(className, constantPoolPredicate, callback);
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
        processorRunner.checkAction(AsmrProcessorAction.READ);
        try {
            AsmrTreeModificationManager.enableModification();
            return new AsmrCopyNodeCaputre<>(node);
        } finally {
            AsmrTreeModificationManager.disableModification();
        }
    }

    public <T extends AsmrNode<T>> AsmrNodeCapture<T> refCapture(T node) {
        processorRunner.checkAction(AsmrProcessorAction.READ);
        return new AsmrReferenceNodeCapture<>(this, node);
    }

    public <T extends AsmrNode<T>> AsmrSliceCapture<T> copyCapture(AsmrAbstractListNode<T, ?> list, int startInclusive, int endExclusive) {
        processorRunner.checkAction(AsmrProcessorAction.READ);
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
        processorRunner.checkAction(AsmrProcessorAction.READ);
        if (startVirtualIndex < 0 || endVirtualIndex >= list.size() * 2 + 2 || endVirtualIndex < startVirtualIndex) {
            throw new IllegalArgumentException(String.format("Virtual [%d, %d), size %d", startVirtualIndex, endVirtualIndex, list.size()));
        }
        return new AsmrReferenceSliceCapture<>(this, list, startVirtualIndex, endVirtualIndex);
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
        processorRunner.checkAction(AsmrProcessorAction.READ);
        if (transformer == null) {
            throw new NullPointerException();
        }
        if (!(target instanceof AsmrReferenceCapture)) {
            throw new IllegalArgumentException("Target must be a reference capture, not a copy capture");
        }
        processorRunner.addWrite(transformer, (AsmrReferenceCapture) target, replacementSupplier, refCaptureInputs);
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
        processorRunner.checkAction(AsmrProcessorAction.READ);
        if (transformer == null) {
            throw new NullPointerException();
        }
        if (!(target instanceof AsmrReferenceCapture)) {
            throw new IllegalArgumentException("Target must be a reference capture, not a copy capture");
        }
        processorRunner.addWrite(transformer, (AsmrReferenceCapture) target, replacementSupplier, refCaptureInputs);
    }

    public void createClass(
            AsmrTransformer transformer,
            String className,
            Supplier<AsmrClassNode> classNodeSupplier
    ) {
        processorRunner.checkAction(AsmrProcessorAction.READ);
        if (transformer == null) {
            throw new NullPointerException();
        }
        if (allClasses.containsKey(className)) {
            throw new IllegalArgumentException("Cannot create class '" + className + "' because it already exists");
        }
        processorRunner.createClass(transformer, className, classNodeSupplier);
    }

    public void deleteClass(AsmrTransformer transformer, String className) {
        processorRunner.checkAction(AsmrProcessorAction.READ);
        if (transformer == null) {
            throw new NullPointerException();
        }
        if (!allClasses.containsKey(className)) {
            throw new IllegalArgumentException("Cannot delete class '" + className + "' because it doesn't exist");
        }
        processorRunner.deleteClass(transformer, className);
    }

    public <T extends AsmrNode<T>> void substitute(T target, AsmrNodeCapture<T> source) {
        processorRunner.checkAction(AsmrProcessorAction.WRITE);
        processorRunner.checkRefCaptureInput(source);

        target.copyFrom(source.resolved(this));
    }

    @SuppressWarnings("unchecked")
    public <E extends AsmrNode<E>, L extends AsmrAbstractListNode<E, L>> void substitute(L target, int index, AsmrSliceCapture<E> source) {
        processorRunner.checkAction(AsmrProcessorAction.WRITE);
        processorRunner.checkRefCaptureInput(source);

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

    List<AsmrTransformer> transformers() {
        return transformers;
    }

    Map<String, ClassProvider> allClasses() {
        return allClasses;
    }

    List<String> phases() {
        return phases;
    }

    Map<String, List<String>> roundDependents() {
        return roundDependents;
    }

    Map<String, List<String>> writeDependents() {
        return writeDependents;
    }

    Set<String> modifiedClasses() {
        return modifiedClasses;
    }

    ConcurrentHashMap<String, ClassInfo> classInfoCache() {
        return classInfoCache;
    }

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
            return new ClassInfo(reader.getSuperName(), (reader.getAccess() & Opcodes.ACC_INTERFACE) != 0);
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
    public void checkWritingClass(String className) {
        processorRunner.checkWritingClass(className);
    }

    static class ClassProvider {
        @Nullable
        private SoftReference<AsmrConstantPool> cachedConstantPool = null;
        @Nullable
        private SoftReference<AsmrClassNode> cachedClass = null;
        @Nullable
        public AsmrClassNode modifiedClass = null;
        @Nullable
        private final InputStreamSupplier inputStreamSupplier; // null for transformed-created classes

        public ClassProvider(@Nullable InputStreamSupplier inputStreamSupplier) {
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

            assert inputStreamSupplier != null : "inputStreamSupplier and modifiedClass should not be null at the same time";
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

    private static class ClassInfo {
        public final String superClass;
        public final boolean isInterface;

        public ClassInfo(String superClass, boolean isInterface) {
            this.superClass = superClass;
            this.isInterface = isInterface;
        }
    }
}
