package org.quiltmc.asmr.processor.verifier;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.quiltmc.asmr.processor.AsmrProcessor;

import java.util.Collection;
import java.util.HashSet;

/**
 * Tells misbehaving transformers to cool down and color between the lines.
 */
// TODO: This could be made significantly faster if we could somehow only scan the constant pool...
public final class FridgeVerifier extends ClassVisitor {
	private static final HashSet<String> FOREBIDDEN_PACKAGES = new HashSet<>();
	private static final HashSet<String> FOREBIDDEN_CLASSES = new HashSet<>();
	private static final HashSet<String> FOREBIDDEN_METHODS = new HashSet<>();
	private static final HashSet<String> FOREBIDDEN_FIELDS = new HashSet<>();

	static {
		loadAutomaticBlacklist();
	}

	@SuppressWarnings("unchecked") // This method is based around a reflection hack, of course we need to cast the raw types.
	private static void loadAutomaticBlacklist() {
		try {
			for (int i = 0; true; i++) {
				Class<?> holder = Class.forName("org.quiltmc.asmr.processor.verifier.generated.AnnotatedElements" + i);
				FOREBIDDEN_PACKAGES.addAll((Collection<String>) holder.getField("PACKAGES").get(null));
				FOREBIDDEN_CLASSES.addAll((Collection<String>) holder.getField("CLASSES").get(null));
				FOREBIDDEN_METHODS.addAll((Collection<String>) holder.getField("METHODS").get(null));
				FOREBIDDEN_FIELDS.addAll((Collection<String>) holder.getField("FIELDS").get(null));
			}
		} catch (ClassNotFoundException e) {
			// we have loaded all of the generated classes
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}
	public FridgeVerifier() {
		super(AsmrProcessor.ASM_VERSION);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
		return new MethodVerifier();
	}

	public static void init() {
	}

	class MethodVerifier extends MethodVisitor {
		public MethodVerifier() {
			super(AsmrProcessor.ASM_VERSION);
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
			if (FOREBIDDEN_PACKAGES.contains(owner.substring(0, owner.lastIndexOf('/'))) || FOREBIDDEN_CLASSES.contains(owner) || FOREBIDDEN_METHODS.contains(owner + name + descriptor)) {
				throw new VerificationException(""); // TODO: print why
			}
			super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
		}

		@Override
		public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
			if (FOREBIDDEN_PACKAGES.contains(owner.substring(0, owner.lastIndexOf('/'))) || FOREBIDDEN_CLASSES.contains(owner) || FOREBIDDEN_FIELDS.contains(owner + name + descriptor)) {
				throw new VerificationException(""); // TODO print why
			}

			super.visitFieldInsn(opcode, owner, name, descriptor);
		}
	}

	public static boolean verify(byte[] classBytes) { // TODO: java 17 frozen arrays
		try {
			ClassReader classReader = new ClassReader(classBytes);
			classReader.accept(new FridgeVerifier(), ClassReader.SKIP_FRAMES);
			return true;
		} catch (VerificationException ex) {
			return false;
		}
	}

	private static final class VerificationException extends RuntimeException {
		public VerificationException(String message) {
			super(message);
		}
	}
}
