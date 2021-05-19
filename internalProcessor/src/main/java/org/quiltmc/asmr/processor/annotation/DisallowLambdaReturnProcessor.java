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
public class DisallowLambdaReturnProcessor extends AbstractProcessor {
	// Includes sub-classes
	List<String> CLASSES = new ArrayList<>();
	// In the format class class + name + descriptor
	List<Desc> FIELDS = new ArrayList<>();
	@Override
	public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
		for (Element annotated : roundEnvironment.getElementsAnnotatedWith(ApiStatus.NonExtendable.class)) {
			if (annotated.getKind().isClass() || annotated.getKind() == ElementKind.INTERFACE) {
				CLASSES.add(getClassName(annotated));
			} else {
				throw new RuntimeException("Cannot process unknown element kind " + annotated.getKind()); // TODO log
			}
		}

		if (CLASSES.isEmpty() && FIELDS.isEmpty()) {
			return false;
		}

		if (roundEnvironment.processingOver()) {
			try {
				JsonWriter writer = JsonWriter.createStrict(processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "generated_lambda_return_blacklist.json").openWriter());
				writer.beginObject();
				writer.name("classes");
				writer.beginArray();
				for (String s : CLASSES) {
					writer.value(s);
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
		a.add(ApiStatus.NonExtendable.class.getCanonicalName());
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
