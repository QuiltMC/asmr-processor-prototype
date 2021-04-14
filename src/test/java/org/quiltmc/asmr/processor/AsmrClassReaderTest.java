package org.quiltmc.asmr.processor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.quiltmc.asmr.processor.tree.AsmrClassListNode;
import org.quiltmc.asmr.processor.tree.AsmrClassNode;
import org.quiltmc.asmr.processor.tree.asmvisitor.AsmrClassVisitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.junit.jupiter.api.Assertions.*;

public class AsmrClassReaderTest {
    @Test
    public void testClassReader() {
        AsmrClassNode classNode = findClass(AsmrClassReaderTest.class);
        // Put a breakpoint here to look at the contents of the class node
        assertEquals(AsmrClassReaderTest.class.getName().replace('.', '/'), classNode.name().value());
    }

    private AsmrClassNode findClass(Class<?> clazz) {
        File sourceLocation;
        try {
            sourceLocation = new File(clazz.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new NoClassDefFoundError(clazz.getName());
        }

        ClassReader classReader;
        try {
            if (sourceLocation.isDirectory()) {
                File classFile = new File(sourceLocation, clazz.getName().replace('.', File.separatorChar) + ".class");
                try (InputStream in = new FileInputStream(classFile)) {
                    classReader = new ClassReader(in);
                }
            } else {
                try (JarFile jar = new JarFile(sourceLocation)) {
                    JarEntry jarEntry = jar.getJarEntry(clazz.getName().replace('.', '/') + ".class");
                    if (jarEntry == null) {
                        throw new NoClassDefFoundError(clazz.getName());
                    }
                    classReader = new ClassReader(jar.getInputStream(jarEntry));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new NoClassDefFoundError(clazz.getName());
        }

        AsmrClassNode classNode = new AsmrClassListNode().add();
        classReader.accept(new AsmrClassVisitor(classNode), ClassReader.SKIP_FRAMES);
        return classNode;
    }
}
