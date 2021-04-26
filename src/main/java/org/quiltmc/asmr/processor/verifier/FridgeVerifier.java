package org.quiltmc.asmr.processor.verifier;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.quiltmc.asmr.processor.AsmrPlatform;
import org.quiltmc.asmr.processor.AsmrProcessor;

/**
 * Tells misbehaving transformers to cool down and color between the lines.
 */
// TODO: This could be made significantly faster if we could somehow only scan the constant pool...
public final class FridgeVerifier extends ClassVisitor {
	static {
		Checker.loadAutomaticBlacklist();
	}

	private String className;

	public FridgeVerifier(AsmrPlatform platform) {
		super(AsmrProcessor.ASM_VERSION);
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		// We force the package to start with "transformer" so that
		if (!name.startsWith("transformer/")) {
			throw new VerificationException("Transformer package name must start with transformer");
		}
		className = name;
		// We restrict the valid superclasses to avoid the transformer somehow gaining access to protected members
		if (!superName.equals("java/lang/Object")) {
			throw new VerificationException("Transformer must extend Object");
		}
		if (interfaces.length > 0) {
			throw new VerificationException("Transformer cannot implement any interfaces");
		}
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
			// Don't let transformers try to call or access other transformers
			if (owner.startsWith("transformer/")) {
				if (owner.equals(className)) {
					// Calling methods in your own transformer is ok
					super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
					return;
				} else {
					throw new VerificationException("Transformer is not allowed to reference other transformers!");
				}
			} else if (!Checker.allowMethod(owner, name, descriptor)) {
				throw new VerificationException(""); // TOOD: context?
			}
			super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
		}

		@Override
		public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
			// Don't let transformers try to call or access other transformers
			if (owner.startsWith("transformer/")) {
				if (owner.equals(className)) {
					// Calling methods in your own transformer is ok
					super.visitFieldInsn(opcode, owner, name, descriptor);
					return;
				} else {
					throw new VerificationException("Transformer is not allowed to reference other transformers!");
				}
			} else if (!Checker.allowField(owner, name, descriptor)) {
				throw new VerificationException(""); // TOOD: context?
			}
			super.visitFieldInsn(opcode, owner, name, descriptor);
		}

		@Override
		public void visitLdcInsn(Object value) {
			if (value instanceof Type) {
				Type type = (Type) value;
				if (!Checker.allowClass(type.getClassName().replace('.', '/'))) {
					throw new VerificationException(""); // TODO: context?
				}
			}
		}
	}

	public static boolean verify(AsmrPlatform platform, byte[] classBytes) { // TODO: java 17 frozen arrays
		try {
			ClassReader classReader = new ClassReader(classBytes);
			classReader.accept(new FridgeVerifier(platform), ClassReader.SKIP_FRAMES);
			return true;
		} catch (VerificationException ex) {
			ex.printStackTrace();
			return false;
		}
	}

	private static final class VerificationException extends RuntimeException {
		public VerificationException(String message) {
			super(message);
		}
	}
}
