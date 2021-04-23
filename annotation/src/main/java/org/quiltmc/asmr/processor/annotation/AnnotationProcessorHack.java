package org.quiltmc.asmr.processor.annotation;

import org.jetbrains.annotations.ApiStatus;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import java.io.*;
import java.util.*;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AnnotationProcessorHack extends AbstractProcessor {
	private int count = 0;
	@Override
	public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
		// Like java packages, subpackages aren't included in this.
		List<String> PACKAGES = new ArrayList<>();
		List<String> CLASSES = new ArrayList<>();
		// In the format class + name + descriptor
		// so org.exampleFoo#foo(com.example2.Bar bar)void becomes org/example/Foofoo(Lcom/example2/Bar;)V
		List<String> METHODS = new ArrayList<>();
		// In the format class class + name + descriptor
		List<String> FIELDS = new ArrayList<>();
		// TODO: everything else we forgot
		for (Element annotated : roundEnvironment.getElementsAnnotatedWith(ApiStatus.Internal.class)) {
			if (annotated.getKind() == ElementKind.PACKAGE) {
				PACKAGES.add(getCanonicalPackageName(annotated));
			} else if (annotated.getKind().isClass()) {
				CLASSES.add(getCanonicalClassName(annotated));
			} else if (annotated.getKind() == ElementKind.METHOD || annotated.getKind() == ElementKind.CONSTRUCTOR) {
				METHODS.add(getFullMethodName(annotated));
			} else if (annotated.getKind() == ElementKind.FIELD) {
				FIELDS.add(getFullFieldName(annotated)); // This intentionally crashes, todo fix
			} else {
				throw new RuntimeException("Cannot process unknown element kind " + annotated.getKind());
			}
		}

		if (PACKAGES.isEmpty() && CLASSES.isEmpty() && METHODS.isEmpty() && FIELDS.isEmpty()) {
			return false;
		}

		try {

			JavaFileObject obj = processingEnv.getFiler().createSourceFile("org.quiltmc.asmr.processor.verifier.generated.AnnotatedElements" + count);
			try (PrintWriter out = new PrintWriter(obj.openWriter())) {
				out.println("package org.quiltmc.asmr.processor.verifier.generated;");
				out.print("public final class AnnotatedElements");
				out.print(count);
				out.println(" {");
				out.println("public static final java.util.HashSet PACKAGES = new java.util.HashSet(); ");
				out.println("public static final java.util.HashSet CLASSES = new java.util.HashSet(); ");
				out.println("public static final java.util.HashSet METHODS = new java.util.HashSet(); ");
				out.println("public static final java.util.HashSet FIELDS = new java.util.HashSet();" );

				out.println("static { ");
				for (String s : PACKAGES) {
					out.println("PACKAGES.add(\"" + s + "\"); ");
				}
				for (String s : CLASSES) {
					out.println("CLASSES.add(\"" + s + "\"); ");
				}
				for (String s : METHODS) {
					out.println("METHODS.add(\"" + s + "\"); ");
				}
				for (String s : FIELDS) {
					out.println("FIELDS.add(\"" + s + "\"); ");
				}
				out.println(" } }");

				out.flush();
				count++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(count);
		return false;
	}


	private static String getCanonicalPackageName(Element pkg) {
		return ((PackageElement) pkg).getQualifiedName().toString().replace('.', '/');
	}
	private static String getCanonicalClassName(Element klass) {
		return ((TypeElement) klass).getQualifiedName().toString().replace('.', '/');
	}

	private static String getFullMethodName(Element method) {
		return getCanonicalClassName(method.getEnclosingElement()) + buildMethodName((ExecutableElement) method);
	}

	private static String buildMethodName(ExecutableElement element) {
		StringBuilder sb = new StringBuilder();
		sb.append(element.getSimpleName());
		sb.append('(');
		for (TypeMirror parameterType : ((ExecutableType) element.asType()).getParameterTypes()) {
			String type = parameterType.toString();
			boolean array = false;
			// First we need to remove arrays and generics
			if (type.endsWith("[]")) {
				type = type.substring(0, type.indexOf('['));
				array = true;
			}

			if (type.contains("<")) {
				type = type.substring(0, type.indexOf('<')); // This will remove all generics
			}

			sb.append(signature(type));
			if (array) {
				sb.append("[");
			}

		}
		sb.append(")");
		sb.append(signature(((ExecutableType) element.asType()).getReturnType().toString()));
		return sb.toString();
	}

	private static String getFullFieldName(Element field) {
		if (!(field instanceof VariableElement)) {
			throw new ClassCastException("Expected variable element");
		}
		return getCanonicalClassName(field.getEnclosingElement()) + field.getSimpleName() + signature(field.asType().toString());
	}

	/**
	 * Returns 'L' to indicate the start of a reference
	 */
	private static String signature(String type) {
		switch (type) {
			case "void":
				return "V";
			case "byte":
				return "B";
			case "char":
				return "C";
			case "double":
				return "D";
			case "float":
				return "F";
			case "int":
				return "I";
			case "long":
				return "J";
			case "short":
				return "S";
			case "boolean":
				return "Z";
			default:
				return "L" + type.replace('.', '/') + ";";
		}
	}
	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Collections.singleton(ApiStatus.Internal.class.getCanonicalName());
	}
}
