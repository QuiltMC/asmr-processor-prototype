package org.quiltmc.asmr.processor.verifier;

import org.objectweb.asm.*;
import org.quiltmc.asmr.processor.AsmrPlatform;
import org.quiltmc.asmr.processor.AsmrProcessor;
import org.quiltmc.asmr.processor.AsmrTransformer;

import java.util.Arrays;

/**
 * Tells misbehaving transformers to cool down and color between the lines.
 */
/*
  Y All classes are blacklisted by default
   Y The transformer class itself (the one that's currently being verified) is whitelisted.
   ~ All classes that ship with the ASMR processor are whitelisted by default. Note that this is not the same as checking the package prefix, as someone may try to get around the restriction by putting their own classes in the same package.
   // ^ is currently implemented at a package level, so technically a classload hack could get another package in. This is very low priority to fix.
   Y Certain JDK classes are whitelisted, such as String, the full list of them may be added to over time.
   Y All classes, methods etc annotated with @ApiStatus.Internal or @HideFromTransformers are blacklisted.
   Y Some JDK methods inside blacklisted classes may be whitelisted, such as System.arraycopy, the full list of them may be added to over time.
   Y Some JDK methods inside whitelisted classes may be blacklisted, such as Math.random, the full list of them may be added to over time.
Y Certain bytecode instructions will be disallowed entirely, such as monitorenter and monitorexit.
Y Certain method modifiers will be disallowed, such as ACC_SYNCHRONIZED and ACC_NATIVE.
- All usages of the invokedynamic instruction will be disallowed except:
   y If the BSM is a member of java/lang/invoke/StringConcatFactory.
   y If the BSM is java/lang/invoke/LambdaMetafactory.metafactory, and:
      - **** The return type additionally is not annotated as @ApiStatus.NotExtendable.
      - **** The lambda captures are either primitive types, from a short list of acceptable JDK classes (e.g. String), or annotated with @AllowLambdaCapture.
y All verified classes shall extend java/lang/Object.
y Verified classes may only have fields with the ACC_FINAL modifier. In addition, these fields shall only be from a short list of allowable types, such as primitive types and String.
   y  all putfield and putstatic instructions whose owner is the current transformer class will be disallowed, unless they are in a <init> or <clinit> method.
      y This is needed in addition to the ACC_FINAL rule because Java 8 does not throw a VerifyError for us.
 */
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
			throw new VerificationException(className, "Transformer package name must start with transformer");
		}
		className = name;
		// We restrict the valid superclasses to avoid the transformer somehow gaining access to protected members
		if (!superName.equals("java/lang/Object")) {
			throw new VerificationException(className, "Transformer must extend Object");
		}
		if (interfaces.length != 1 || !interfaces[0].equals("org/quiltmc/asmr/processor/AsmrTransformer")) {
			throw new VerificationException(className, "Transformer cannot implement any interfaces");
		}
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
		if ((access & Opcodes.ACC_NATIVE) != 0) {
			throw new VerificationException(className, "Transformer cannot have native methods");
		} else if ((access & Opcodes.ACC_SYNCHRONIZED) != 0) {
			throw new VerificationException(className, "Transformer cannot have synchronized methods");
		} else {
			return new MethodVerifier(name + descriptor);
		}
	}

	public static void init() {
	}

	class MethodVerifier extends MethodVisitor {
		String methodName;
		public MethodVerifier(String methodName) {
			super(AsmrProcessor.ASM_VERSION);
			this.methodName = methodName;
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
					throw new VerificationException(className, methodName, "A transformer is not allowed to reference other transformers!");
				}
			} else if (!Checker.allowMethod(owner, name, descriptor)) {
				throw new VerificationException(className, "Method " + owner + "::" + name + descriptor + " is not whitelisted!");
			}
			super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
		}

		@Override
		public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
			// Don't let transformers try to call or access other transformers
			if (owner.startsWith("transformer/")) {
				if (owner.equals(className)) {
					// Your fields are supposed to be final!
					throw new VerificationException(className, methodName, "All references to a transformer's own fields must be inlined");
				} else {
					throw new VerificationException(className, methodName, "A transformer is not allowed to reference other transformers!");
				}
			} else if (!Checker.allowField(owner, name, descriptor)) {
				throw new VerificationException(className, "Field " + owner + "::" + name + descriptor + " is not whitelisted!");
			}
			super.visitFieldInsn(opcode, owner, name, descriptor);
		}

		@Override
		public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
			if (bootstrapMethodHandle.getOwner().equals("java/lang/invoke/StringConcatFactory")) {
				super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
			} else if (bootstrapMethodHandle.getOwner().equals("java/lang/invoke/LambdaMetafactory")) {
				// TODO: check the captures
				Handle target = ((Handle) bootstrapMethodArguments[1]);
				if (target.getOwner().equals(className)) {
					super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
					return;
				}

				if (!Checker.allowClass(target.getOwner())) {
					throw new VerificationException(className, methodName, "");
				}
				// TODO: check the method being called
			} else {
				throw new VerificationException(className, methodName, "");
			}
		}

		@Override
		public void visitLdcInsn(Object value) {
			if (value instanceof Type) {
				Type type = (Type) value;
				if (!Checker.allowClass(type.getClassName().replace('.', '/'))) {
					throw new VerificationException(className, methodName, "Class constant referenced in LDC is not whitelisted: " + type.getClassName());
				}
			}

			super.visitLdcInsn(value);
		}

		@Override
		public void visitInsn(int opcode) {
			if (opcode == Opcodes.MONITORENTER || opcode == Opcodes.MONITOREXIT) {
				throw new VerificationException(className, methodName, "Synchronization is not allowed");
			}
			super.visitInsn(opcode);
		}
	}

	public static boolean verify(AsmrPlatform platform, byte[] classBytes) {
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
		public VerificationException(String clazzName, String methodName, String message) {
			super(clazzName + "::" + methodName + " is not a valid transformer: "+ message);
		}

		public VerificationException(String clazzName, String message) {
			super(clazzName + " is not a valid transformer: " + message);
		}
	}
}
