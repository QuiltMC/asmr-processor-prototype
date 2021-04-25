package org.quiltmc.asmr.processor;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
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

	static void findClass(Class<?> clazz, InputStreamConsumer inputConsumer) {
		File sourceLocation;
		try {
			sourceLocation = new File(clazz.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new NoClassDefFoundError(clazz.getName());
		}

		try {
			if (sourceLocation.isDirectory()) {
				File classFile = new File(sourceLocation, clazz.getName().replace('.', File.separatorChar) + ".class");
				try (InputStream in = new FileInputStream(classFile)) {
					inputConsumer.accept(in);
				}
			} else {
				try (JarFile jar = new JarFile(sourceLocation)) {
					JarEntry jarEntry = jar.getJarEntry(clazz.getName().replace('.', '/') + ".class");
					if (jarEntry == null) {
						throw new NoClassDefFoundError(clazz.getName());
					}
					inputConsumer.accept(jar.getInputStream(jarEntry));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new NoClassDefFoundError(clazz.getName());
		}
	}

	static AsmrClassNode findClass(Class<?> clazz) {
		ClassReader[] classReader = new ClassReader[1];
		findClass(clazz, inputStream -> classReader[0] = new ClassReader(inputStream));

		AsmrClassNode classNode = new AsmrClassNode(null);
		classReader[0].accept(new AsmrClassVisitor(classNode), ClassReader.SKIP_FRAMES);
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

	@FunctionalInterface
	interface InputStreamConsumer {
		void accept(InputStream inputStream) throws IOException;
	}
}
