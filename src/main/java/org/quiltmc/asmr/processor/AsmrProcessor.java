package org.quiltmc.asmr.processor;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.quiltmc.asmr.processor.tree.AsmrClassListNode;
import org.quiltmc.asmr.processor.tree.AsmrClassNode;
import org.quiltmc.asmr.processor.tree.asmvisitor.AsmrClassVisitor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class AsmrProcessor {
    public static final int ASM_VERSION = Opcodes.ASM9;

    private final AsmrPlatform platform;
    private final AsmrClassListNode classes = new AsmrClassListNode();

    public AsmrProcessor(AsmrPlatform platform) {
        this.platform = platform;
    }

    public void addJar(Path jar, boolean readOnly) {
        try (JarFile jarFile = new JarFile(jar.toFile())) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                if (jarEntry.getName().endsWith(".class")) {
                    doAddClass(new ClassReader(jarFile.getInputStream(jarEntry)), readOnly);
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Jar could not be read", e);
        }
    }

    public void addClass(byte[] bytecode, boolean readOnly) {
        invalidateCache();
        doAddClass(new ClassReader(bytecode), readOnly);
    }

    private void doAddClass(ClassReader classReader, boolean readOnly) {
        AsmrClassNode classNode = classes.add();
        classReader.accept(new AsmrClassVisitor(classNode), ClassReader.EXPAND_FRAMES);
    }

    public void invalidateCache() {

    }
}
