package org.quiltmc.asmr.processor.annotation.test;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class VerifierAnnotationProcessor extends AbstractProcessor {
	private PrintWriter writer;
	@Override
	public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
		if (writer == null) {
			try {
				writer = new PrintWriter(processingEnv.getFiler().createSourceFile("org.quiltmc.asmr.processor.test.VerifierTests").openWriter());
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}

			writer.write("package org.quiltmc.asmr.processor.test;class VerifierTests {");
		}
		for (Element annotated : roundEnvironment.getElementsAnnotatedWith(VerifierTests.Fails.class)) {
			TypeElement element = (TypeElement) annotated;
			writer.write("@org.junit.jupiter.api.Test void ");
			writer.write(annotated.getSimpleName().toString().substring(0, 1).toLowerCase(Locale.US));
			writer.write(annotated.getSimpleName().toString().substring(1));
			writer.write("() {" +
					" org.junit.jupiter.api.Assertions.assertFalse(org.quiltmc.asmr.processor.verifier.FridgeVerifier" +
					".verify(new org.quiltmc.asmr.processor.test.AsmrTestPlatform(), org.quiltmc.asmr.processor.test.AsmrClassTestUtil.findClassBytes(");
			writer.write(element.getQualifiedName().toString());
			writer.write(".class)));}");
		}

		for (Element annotated : roundEnvironment.getElementsAnnotatedWith(VerifierTests.Passes.class)) {
			TypeElement element = (TypeElement) annotated;
			writer.write("@org.junit.jupiter.api.Test void ");
			writer.write(annotated.getSimpleName().toString().substring(0, 1).toLowerCase(Locale.US));
			writer.write(annotated.getSimpleName().toString().substring(1));
			writer.write("() {" +
					" org.junit.jupiter.api.Assertions.assertTrue(org.quiltmc.asmr.processor.verifier.FridgeVerifier" +
					".verify(new org.quiltmc.asmr.processor.test.AsmrTestPlatform(), org.quiltmc.asmr.processor.test.AsmrClassTestUtil.findClassBytes(");
			writer.write(element.getQualifiedName().toString());
			writer.write(".class)));}");
		}

		if (roundEnvironment.processingOver()) {
			writer.write("}");
			writer.flush();
			writer.close();
		}
		return false;
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		Set<String> annotations = new HashSet<>(2);
		annotations.add(VerifierTests.Fails.class.getCanonicalName());
		annotations.add(VerifierTests.Passes.class.getCanonicalName());
		return annotations;
	}
}
