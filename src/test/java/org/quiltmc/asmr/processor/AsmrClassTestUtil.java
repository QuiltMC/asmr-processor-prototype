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
import java.nio.file.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class AsmrClassTestUtil {
	private AsmrClassTestUtil() {
	}

	public static byte[] findClassBytes(Class<?> clazz) {
		Path sourceLocation;
		try {
			sourceLocation = Paths.get(clazz.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new NoClassDefFoundError(clazz.getName());
		}

		byte[] bytes;
		try {
			if (Files.isDirectory(sourceLocation)) {
				Path classFile = sourceLocation.resolve(clazz.getName().replace('.', File.separatorChar) + ".class");
				bytes = Files.readAllBytes(classFile);
			} else {
				try (FileSystem fs = FileSystems.newFileSystem(sourceLocation, (ClassLoader) null);) {
					Path jarEntry = fs.getPath(clazz.getName().replace('.', '/') + ".class");
					bytes = Files.readAllBytes(jarEntry);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new NoClassDefFoundError(clazz.getName());
		}

		return bytes;
	}
	public static AsmrClassNode findClass(Class<?> clazz) {
		ClassReader reader = new ClassReader(findClassBytes(clazz));
		AsmrClassNode classNode = new AsmrClassListNode().add();
		reader.accept(new AsmrClassVisitor(classNode), ClassReader.SKIP_FRAMES);
		return classNode;
	}
}
