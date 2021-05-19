package org.quiltmc.asmr.processor.annotation;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.json5.JsonWriter;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.*;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AllowLambdaCaptureProcessor extends AbstractProcessor {
	// Includes sub-classes
	List<String> CLASSES = new ArrayList<>();
	// In the format class class + name + descriptor
	List<Desc> FIELDS = new ArrayList<>();
	@Override
	public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
		// TODO: everything else we forgot
		for (Element annotated : roundEnvironment.getElementsAnnotatedWith(AllowLambdaCapture.class)) {
			if (annotated.getKind().isClass() || annotated.getKind() == ElementKind.INTERFACE) {
				CLASSES.add(getClassName(annotated));
			} else if (annotated.getKind() == ElementKind.FIELD) {
				FIELDS.add(getFullFieldName(annotated));
			} else {
				throw new RuntimeException("Cannot process unknown element kind " + annotated.getKind()); // TODO log
			}
		}

		if (CLASSES.isEmpty() && FIELDS.isEmpty()) {
			return false;
		}

		if (roundEnvironment.processingOver()) {
			try {
				JsonWriter writer = JsonWriter.createStrict(processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "generated_capture_whitelist.json").openWriter());
				writer.beginObject();
				writer.name("classes");
				writer.beginArray();
				for (String s : CLASSES) {
					writer.value(s);
				}
				writer.endArray();
				// TODO: code duplication
				writer.name("fields");
				writer.beginArray();
				for (Desc desc : FIELDS) {
					writer.beginObject()
							.name("owner")
							.value(desc.owner)
							.name("name")
							.value(desc.name)
							.name("descriptor")
							.value(desc.descriptor)
							.endObject();
				}
				writer.endArray();
				writer.endObject();
				writer.flush();
				writer.close();
			} catch (IOException e) {
				throw new RuntimeException(); // TODO: log
			}
		}

		return false;
	}

	private static String getClassName(Element klass) {
		if (klass.getEnclosingElement() instanceof PackageElement) {
 			return ((TypeElement) klass).getQualifiedName().toString().replace('.', '/');
		}

		String first;
		List<String> inners = new ArrayList<>();
		TypeElement parent = (TypeElement) klass;
		while (true) {
			if (parent.getEnclosingElement() instanceof PackageElement) {
				first = parent.getQualifiedName().toString();
				break;
			} else {
				inners.add(parent.getSimpleName().toString());
				parent = (TypeElement) parent.getEnclosingElement();
			}
		}

		Collections.reverse(inners);
		StringBuilder sb = new StringBuilder(first.replace('.', '/'));
		for (String inner : inners) {
			sb.append('$').append(inner.replace('.', '/'));
		}
		return sb.toString();
	}

	private static String getMethodDesc(ExecutableElement element) {
		StringBuilder sb = new StringBuilder();
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

	private static Desc getFullFieldName(Element field) {
		if (!(field instanceof VariableElement)) {
			throw new ClassCastException("Expected variable element");
		}
		return new Desc(getClassName(field.getEnclosingElement()),  field.getSimpleName().toString(), signature(field.asType().toString()));
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
		HashSet<String> a = new HashSet<>();
		a.add(AllowLambdaCapture.class.getCanonicalName());
		return a;
	}

	public static final class Desc {
		final String owner;
		final String name;
		final String descriptor;

		Desc(String owner, String name, String descriptor) {
			this.owner = owner;
			this.name = name;
			this.descriptor = descriptor;
		}
	}
}
