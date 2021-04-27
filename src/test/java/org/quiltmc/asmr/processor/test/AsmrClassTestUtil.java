package org.quiltmc.asmr.processor.test;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.quiltmc.asmr.processor.AsmrClassWriter;
import org.quiltmc.asmr.processor.AsmrProcessor;
import org.quiltmc.asmr.processor.tree.member.AsmrClassNode;
import org.quiltmc.asmr.processor.tree.asmvisitor.AsmrClassVisitor;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
		AsmrClassNode classNode = new AsmrClassNode();
		reader.accept(new AsmrClassVisitor(classNode), ClassReader.SKIP_FRAMES);
		return classNode;
	}

	static Class<?> defineClass(AsmrProcessor processor, AsmrClassNode classNode) {
		String className = classNode.name().value().replace('/', '.');

		ClassWriter writer = new AsmrClassWriter(processor);
		classNode.accept(writer);
		byte[] bytecode = writer.toByteArray();

		ClassLoader classLoader = new ClassLoader(Thread.currentThread().getContextClassLoader()) {
			private Class<?> customClass = null;

			@Override
			protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
				Class<?> loadedClass = findLoadedClass(name);
				if (loadedClass == null) {
					try {
						loadedClass = findClass(name);
					} catch (ClassNotFoundException ignore) {
						return super.loadClass(name, resolve);
					}
				}

				if (resolve) {
					resolveClass(loadedClass);
				}
				return loadedClass;
			}

			@Override
			protected Class<?> findClass(String name) throws ClassNotFoundException {
				if (className.equals(name)) {
					if (customClass == null) {
						customClass = defineClass(className, bytecode, 0, bytecode.length);
					}
					return customClass;
				}
				return super.findClass(name);
			}
		};
		try {
			return classLoader.loadClass(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
