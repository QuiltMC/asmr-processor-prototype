package org.quiltmc.asmr.processor;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.quiltmc.asmr.processor.capture.AllowLambdaCapture;
import org.quiltmc.asmr.processor.capture.AsmrCopyNodeCaputre;
import org.quiltmc.asmr.processor.capture.AsmrCopySliceCapture;
import org.quiltmc.asmr.processor.capture.AsmrNodeCapture;
import org.quiltmc.asmr.processor.capture.AsmrReferenceCapture;
import org.quiltmc.asmr.processor.capture.AsmrReferenceNodeCapture;
import org.quiltmc.asmr.processor.capture.AsmrReferenceSliceCapture;
import org.quiltmc.asmr.processor.capture.AsmrSliceCapture;
import org.quiltmc.asmr.processor.tree.AsmrAbstractListNode;
import org.quiltmc.asmr.processor.tree.AsmrNode;
import org.quiltmc.asmr.processor.tree.AsmrValueNode;
import org.quiltmc.asmr.processor.tree.asmvisitor.AsmrClassVisitor;
import org.quiltmc.asmr.processor.tree.member.AsmrClassNode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@AllowLambdaCapture
public class AsmrProcessor implements AutoCloseable {
    public static final int ASM_VERSION = Opcodes.ASM9;

    private final AsmrPlatform platform;

    private final List<JarFile> jarFiles = new ArrayList<>();

    private final List<AsmrTransformer> transformers = new ArrayList<>();
    private final TreeMap<String, ClassProvider> allClasses = new TreeMap<>();
    private final TreeMap<String, String> config = new TreeMap<>();

    private ConcurrentHashMap<String, ConcurrentLinkedQueue<Consumer<AsmrClassNode>>> requestedClasses = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<AsmrReferenceCapture>> referenceCaptures = new ConcurrentHashMap<>();
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

        // TODO: compute a loop of these
        runReadWriteRound();
    }

    private void runReadWriteRound() {
        // read phase
        transformers.parallelStream().forEach(transformer -> transformer.read(this));

        while (!this.requestedClasses.isEmpty()) {
            ConcurrentHashMap<String, ConcurrentLinkedQueue<Consumer<AsmrClassNode>>> requestedClasses = this.requestedClasses;
            this.requestedClasses = new ConcurrentHashMap<>();
            requestedClasses.entrySet().parallelStream().forEach(entry -> {
                String className = entry.getKey();
                ConcurrentLinkedQueue<Consumer<AsmrClassNode>> callbacks = entry.getValue();
                AsmrClassNode classNode;
                try {
                    classNode = allClasses.get(className).get();
                } catch (IOException e) {
                    throw new UncheckedIOException("Error reading class, did it get deleted?", e);
                }
                for (Consumer<AsmrClassNode> callback : callbacks) {
                    callback.accept(classNode);
                }
            });
        }

        // TODO: detect conflicts

        // write phase
        writes.entrySet().parallelStream().forEach(entry -> {
            String className = entry.getKey();
            ConcurrentLinkedQueue<Write> writes = entry.getValue();

            try {
                ClassProvider classProvider = allClasses.get(className);
                classProvider.modifiedClass = classProvider.get();
            } catch (IOException e) {
                throw new UncheckedIOException("Error reading class, did it get deleted?", e);
            }

            ConcurrentLinkedQueue<AsmrReferenceCapture> refCaptures = referenceCaptures.get(className);
            if (refCaptures != null) {
                for (AsmrReferenceCapture refCapture : refCaptures) {
                    refCapture.computeResolved(this);
                }
            }

            // TODO: sort writes by processor
            for (Write write : writes) {
                if (write.target instanceof AsmrNodeCapture) {
                    copyFrom(((AsmrNodeCapture<?>) write.target).resolved(), write.replacementSupplier.get());
                } else {
                    AsmrSliceCapture<?> sliceCapture = (AsmrSliceCapture<?>) write.target;
                    AsmrAbstractListNode<?, ?> list = sliceCapture.resolvedList();
                    int startIndex = sliceCapture.startNodeInclusive();
                    int endIndex = sliceCapture.endNodeExclusive();
                    list.remove(startIndex, endIndex);
                    insertCopy(list, startIndex, (AsmrAbstractListNode<?, ?>) write.replacementSupplier.get());
                }
            }
        });

        modifiedClasses.addAll(writes.keySet());
        for (String className : writes.keySet()) {
            classInfoCache.remove(className);
        }

        writes.clear();
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

    public boolean classExists(String name) {
        return allClasses.containsKey(name);
    }

    public void withClass(String name, Consumer<AsmrClassNode> callback) {
        if (!allClasses.containsKey(name)) {
            throw new IllegalArgumentException("Class not found: " + name);
        }
        requestedClasses.computeIfAbsent(name, k -> new ConcurrentLinkedQueue<>()).add(callback);
    }

    public void withClasses(Predicate<String> namePredicate, Consumer<AsmrClassNode> callback) {
        for (String className : allClasses.keySet()) {
            if (namePredicate.test(className)) {
                requestedClasses.computeIfAbsent(className, k -> new ConcurrentLinkedQueue<>()).add(callback);
            }
        }
    }

    public void withClasses(String prefix, Consumer<AsmrClassNode> callback) {
        withClasses(name -> name.startsWith(prefix), callback);
    }

    public void withAllClasses(Consumer<AsmrClassNode> callback) {
        withClasses(name -> true, callback);
    }

    public <T extends AsmrNode<T>> AsmrNodeCapture<T> copyCapture(T node) {
        return new AsmrCopyNodeCaputre<>(node);
    }

    public <T extends AsmrNode<T>> AsmrNodeCapture<T> refCapture(T node) {
        AsmrReferenceNodeCapture<T> capture = new AsmrReferenceNodeCapture<>(node);
        referenceCaptures.computeIfAbsent(capture.className(), k -> new ConcurrentLinkedQueue<>()).add(capture);
        return capture;
    }

    public <T extends AsmrNode<T>> AsmrSliceCapture<T> copyCapture(AsmrAbstractListNode<T, ?> list, int startInclusive, int endExclusive) {
        return new AsmrCopySliceCapture<>(list, startInclusive, endExclusive);
    }

    public <T extends AsmrNode<T>> AsmrSliceCapture<T> refCapture(AsmrAbstractListNode<T, ?> list, int startIndex, int endIndex, boolean startInclusive, boolean endInclusive) {
        AsmrReferenceSliceCapture<T, ?> capture = new AsmrReferenceSliceCapture<>(list, startIndex, endIndex, startInclusive, endInclusive);
        referenceCaptures.computeIfAbsent(capture.className(), k -> new ConcurrentLinkedQueue<>()).add(capture);
        return capture;
    }

    @SuppressWarnings("unchecked")
    public <T extends AsmrNode<T>> void addWrite(AsmrTransformer transformer, AsmrNodeCapture<T> target, Supplier<? extends T> replacementSupplier) {
        if (transformer == null) {
            throw new NullPointerException();
        }
        if (!(target instanceof AsmrReferenceCapture)) {
            throw new IllegalArgumentException("Target must be a reference capture, not a copy capture");
        }
        AsmrReferenceCapture refTarget = (AsmrReferenceCapture) target;
        Write write = new Write(transformer, refTarget, (Supplier<AsmrNode<?>>) replacementSupplier);
        writes.computeIfAbsent(refTarget.className(), k -> new ConcurrentLinkedQueue<>()).add(write);
    }

    @SuppressWarnings("unchecked")
    public <T extends AsmrNode<T>, L extends AsmrAbstractListNode<T, L>> void addWrite(AsmrTransformer transformer, AsmrSliceCapture<T> target, Supplier<L> replacementSupplier) {
        if (transformer == null) {
            throw new NullPointerException();
        }
        if (!(target instanceof AsmrReferenceCapture)) {
            throw new IllegalArgumentException("Target must be a reference capture, not a copy capture");
        }
        AsmrReferenceCapture refTarget = (AsmrReferenceCapture) target;
        Write write = new Write(transformer, refTarget, (Supplier<AsmrNode<?>>) (Supplier<?>) replacementSupplier);
        writes.computeIfAbsent(refTarget.className(), k -> new ConcurrentLinkedQueue<>()).add(write);
    }

    @Nullable
    public String getConfigValue(String key) {
        return config.get(key);
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

            class SuperclassClassVisitor extends ClassVisitor {
                String superName;
                boolean isInterface;

                SuperclassClassVisitor() {
                    super(ASM_VERSION);
                }

                @Override
                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                    this.superName = superName;
                    this.isInterface = (access & Opcodes.ACC_INTERFACE) != 0;
                }
            }

            SuperclassClassVisitor cv = new SuperclassClassVisitor();
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

    private static class ClassProvider {
        private WeakReference<AsmrClassNode> cachedClass = null;
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
            AsmrClassNode val = new AsmrClassNode(null);
            classReader.accept(new AsmrClassVisitor(val), ClassReader.SKIP_FRAMES);
            cachedClass = new WeakReference<>(val);
            return val;
        }
    }

    @FunctionalInterface
    private interface InputStreamSupplier {
        InputStream get() throws IOException;
    }

    private static class Write {
        public final AsmrTransformer transformer;
        public final AsmrReferenceCapture target;
        public final Supplier<AsmrNode<?>> replacementSupplier;

        public Write(AsmrTransformer transformer, AsmrReferenceCapture target, Supplier<AsmrNode<?>> replacementSupplier) {
            this.transformer = transformer;
            this.target = target;
            this.replacementSupplier = replacementSupplier;
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
