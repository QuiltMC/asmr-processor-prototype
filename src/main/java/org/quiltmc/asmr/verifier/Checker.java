package org.quiltmc.asmr.verifier;

import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.Type;
import org.quiltmc.asmr.processor.annotation.AllowLambdaCapture;
import org.quiltmc.json5.JsonReader;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

final class Checker {
	private Checker() {
	}


	// classes
	private static final ArrayList<String> WHITELIST_PACKAGE_START = new ArrayList<>();
	static {
		WHITELIST_PACKAGE_START.add("org/quiltmc/asmr/processor");
	}
	private static final HashSet<String> WHITELIST_PACKAGE_EXACT = new HashSet<>();
	private static final HashSet<String> WHITELIST_CLASS_EXACT = new HashSet<>();
	static {
		WHITELIST_CLASS_EXACT.add("java/lang/Object");
	}
	private static final HashSet<String> WHITELIST_CLASS_TREE = new HashSet<>();

	private static final ArrayList<String> BLACKLIST_PACKAGE_START = new ArrayList<>();
	private static final HashSet<String> BLACKLIST_PACKAGE_EXACT = new HashSet<>();
	private static final HashSet<String> BLACKLIST_CLASS_EXACT = new HashSet<>();
	private static final HashSet<String> BLACKLIST_CLASS_TREE = new HashSet<>();

	// methods
	private static final HashSet<VerificationDesc> WHITELIST_METHOD_EXACT = new HashSet<>();
	private static final HashSet<VerificationDesc> WHITELIST_METHOD_TREE = new HashSet<>(); // TODO: bridge methods?

	private static final HashSet<VerificationDesc> BLACKLIST_METHOD_EXACT = new HashSet<>();
	private static final HashSet<VerificationDesc> BLACKLIST_METHOD_TREE = new HashSet<>();

	// Always tree because reasons
	private static final HashSet<VerificationDesc> BLACKLIST_FIELDS = new HashSet<>();
	private static final HashSet<VerificationDesc> WHITELIST_FIELDS = new HashSet<>();

	private static final HashSet<String> WHITELIST_LAMBDA_CAPTURE_CLASSES = new HashSet<>();
	static {
		WHITELIST_LAMBDA_CAPTURE_CLASSES.add("java/lang/String");
	}

	static boolean allowClass(String clazz) {
		try {
			return classWhitelisted(clazz) && !classBlacklisted(clazz);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e); // TODO: remove
		}
	}
	static boolean allowMethod(String owner, String name, String descriptor) {
		try {
			return methodWhitelisted(owner, name, descriptor) || (!classBlacklisted(owner) && classWhitelisted(owner) && !methodBlacklisted(owner, name, descriptor));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e); // TODO: remove
		}
	}

	static boolean allowField(String owner, String name, String descriptor) {
		try {
			return fieldWhitelisted(owner, name, descriptor) || (!classBlacklisted(owner) && classWhitelisted(owner) && !fieldBlacklisted(owner, name, descriptor));
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e); // TODO: remove
		}
	}

	static boolean allowLambdaCapture(String name) {
		try {
			return typeAllowedForLambdaCapture(name);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e); // TODO: remove
		}
	}

	static boolean allowLambdaReturn(String descriptor) {
		try {
			return classAllowedForLambdaReturn(descriptor);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e); // TODO: remove
		}
	}

	// TODO: we can probably cache this?
	private static boolean classWhitelisted(String name) throws ClassNotFoundException {
		boolean whitelisted = false;
		for (String pkg : WHITELIST_PACKAGE_START) {
			if (name.startsWith(pkg)) {
				whitelisted = true;
				break;
			}
		}
		whitelisted = whitelisted || WHITELIST_PACKAGE_EXACT.contains(name.substring(0, name.lastIndexOf('/')));
		whitelisted = whitelisted || WHITELIST_CLASS_EXACT.contains(name);
		Class<?> cl = Class.forName(name.replace('/', '.'));
		do {
			if (WHITELIST_CLASS_TREE.contains(cl.getName().replace('.', '/'))) {
				whitelisted = true;
				break;
			}
		} while ((cl = cl.getSuperclass()) != null);
		return whitelisted;
	}

	private static boolean classBlacklisted(String name) throws ClassNotFoundException {
		// Blacklists override whitelists

		if (BLACKLIST_PACKAGE_EXACT.contains(name.substring(0, name.lastIndexOf('/')))) {
			return true;
		}

		if (BLACKLIST_CLASS_EXACT.contains(name)) {
			return true;
		}

		for (String pkg : BLACKLIST_PACKAGE_START) {
			if (name.startsWith(pkg)) {
				return true;
			}
		}


		Class<?> cl = Class.forName(name.replace('/', '.'));
		do {
			if (BLACKLIST_CLASS_TREE.contains(cl.getName().replace('.', '/'))) {
				return true;
			}
		} while ((cl = cl.getSuperclass()) != null);

		return false;
	}

	private static boolean methodWhitelisted(String clazz, String name, String descriptor) throws ClassNotFoundException {
		VerificationDesc desc = new VerificationDesc(clazz, name, descriptor);
		if (WHITELIST_METHOD_EXACT.contains(desc)) {
			return true;
		}

		Class<?> cl = Class.forName(clazz.replace('/', '.'));
		// TODO: there is a better way of doing this, i'm sure.
		// 	maybe getDeclaringClass can help? but we still need to do an ugly search if it gets overridden
		do {
			for (Method method : cl.getDeclaredMethods()) {
				if (method.getName().equals(name) && WHITELIST_METHOD_TREE.contains(new VerificationDesc(cl.getName().replace('.', '/'), method.getName(), getMethodVerificationDescriptor(method)))) {
					return true;
				}
			}
		} while ((cl = cl.getSuperclass()) != null);

		return false;
	}

	private static boolean methodBlacklisted(String clazz, String name, String descriptor) throws ClassNotFoundException {
		VerificationDesc desc = new VerificationDesc(clazz, name, descriptor);
		if (BLACKLIST_METHOD_EXACT.contains(desc)) {
			return true;
		}

		Class<?> cl = Class.forName(clazz.replace('/', '.'));
		// TODO: there is a better way of doing this, i'm sure.
		// 	maybe getDeclaringClass can help? but we still need to do an ugly search if it gets overridden
		do {
			for (Method method : cl.getDeclaredMethods()) {
				VerificationDesc d = new VerificationDesc(cl.getName().replace('.', '/'), method.getName(), getMethodVerificationDescriptor(method));
				if (method.getName().equals(name)) {
					if (BLACKLIST_METHOD_TREE.contains(d)) {
						return true;
					}

				}
			}
		} while ((cl = cl.getSuperclass()) != null);

		return false;
	}

	// TODO: these ignore field descriptors for now
	//		not going to worry about it until it becomes an issue
	private static boolean fieldWhitelisted(String clazz, String name, String descriptor) throws ClassNotFoundException, NoSuchFieldException {
		Class<?> cl = Class.forName(clazz.replace('/', '.'));
		String realOwner = cl.getField(name).getClass().getName().replace('.', '/');
		return WHITELIST_FIELDS.contains(new VerificationDesc(realOwner, name, descriptor));
	}

	private static boolean fieldBlacklisted(String clazz, String name, String descriptor) throws ClassNotFoundException, NoSuchFieldException {
		Class<?> cl = Class.forName(clazz.replace('/', '.'));
		String realOwner = cl.getField(name).getDeclaringClass().getName().replace('.', '/');
		return BLACKLIST_FIELDS.contains(new VerificationDesc(realOwner, name, descriptor));
	}

	private static boolean typeAllowedForLambdaCapture(String name) throws ClassNotFoundException {
		if ("VZCBSIFJD".contains(name)) {
			return true;
		}

		if (WHITELIST_LAMBDA_CAPTURE_CLASSES.contains(name)) {
			return true;
		}

		Class<?> cl = Class.forName(name.replace('/', '.'));
		return cl.isAnnotationPresent(AllowLambdaCapture.class);
	}

	private static boolean classAllowedForLambdaReturn(String name) throws ClassNotFoundException {
		Class<?> cl = Class.forName(name.replace('/', '.'));
		return !cl.isAnnotationPresent(ApiStatus.Internal.class);
	}

	static String getVerificationDescriptorForClass(final Class c) {
		if (c.isPrimitive()) {
			if(c == byte.class) {
				return "B";
			}

			if(c==char.class)  {
				return "C";
			}
			if(c==double.class) {
				return "D";
			}
			if(c==float.class) {
				return "F";
			}
			if(c==int.class) {
				return "I";
			}

			if(c==long.class) {
				return "J";
			}

			if(c==short.class) {
				return "S";
			}

			if(c==boolean.class) {
				return "Z";
			}

			if(c==void.class) {
				return "V";
			}
			throw new RuntimeException("Unrecognized primitive " + c);
		}

		if (c.isArray()) {
			return c.getName().replace('.', '/');
		}
		return ('L' + c.getName() + ';').replace('.', '/');
	}

	static String getMethodVerificationDescriptor(Method m) {
		StringBuilder s= new StringBuilder("(");
		for(final Class<?> c: m.getParameterTypes()) {
			s.append(getVerificationDescriptorForClass(c));
		}

		s.append(')');
		return s.append(getVerificationDescriptorForClass(m.getReturnType())).toString();
	}


	static void loadAutomaticBlacklist() {
		// TODO: use asmrplatform to load this
		InputStream stream = ClassLoader.getSystemResourceAsStream("generated_blacklist.json");
		if (stream == null) {
			throw new IllegalStateException("Cannot load generated blacklist from annotation processor");
		}
		JsonReader reader = JsonReader.createStrict(new BufferedReader(new InputStreamReader(stream)));
		try {
			reader.beginObject();
			while(reader.hasNext()) {
				switch (reader.nextName()) {
					case "packages":
						reader.beginArray();
						while (reader.hasNext()) {
							BLACKLIST_PACKAGE_EXACT.add(reader.nextString());
						}
						reader.endArray();
						break;
					case "classes":
						reader.beginArray();
						while (reader.hasNext()) {
							BLACKLIST_CLASS_TREE.add(reader.nextString());
						}
						reader.endArray();
						break;
					case "methods":
						reader.beginArray();
						while (reader.hasNext()) {
							BLACKLIST_METHOD_TREE.add(buildVerificationDesc(reader));
						}
						reader.endArray();
						break;
					case "fields":
						reader.beginArray();
						while (reader.hasNext()) {
							BLACKLIST_FIELDS.add(buildVerificationDesc(reader));
						}
						reader.endArray();
						break;
				}
			}
			reader.endObject();
			reader.close();
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private static VerificationDesc buildVerificationDesc(JsonReader reader) throws IOException {
		reader.beginObject();
		String owner = null;
		String name = null;
		String desc = null;
		while (reader.hasNext()) {
			switch (reader.nextName()) {
				case "owner":
					owner = reader.nextString();
					break;
				case "name":
					name = reader.nextString();
					break;
				case "descriptor":
					desc = reader.nextString();
					break;
			}
		}
		reader.endObject();
		return new VerificationDesc(Objects.requireNonNull(owner), Objects.requireNonNull(name), Objects.requireNonNull(desc));
	}
}
