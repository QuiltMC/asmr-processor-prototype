package org.quiltmc.asmr.processor;

import org.objectweb.asm.ClassReader;
import org.quiltmc.asmr.processor.tree.member.AsmrClassListNode;
import org.quiltmc.asmr.processor.tree.member.AsmrClassNode;
import org.quiltmc.asmr.processor.tree.asmvisitor.AsmrClassVisitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class AsmrClassTestUtil {
	private AsmrClassTestUtil() {
	}

	static AsmrClassNode findClass(Class<?> clazz) {
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
