package org.quiltmc.asmr.processor.annotation.test;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.Set;

public class VerifierAnnotationProcessor extends AbstractProcessor {
	private PrintWriter writer;
	@Override
	public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
		if (writer == null) {
			try {
				writer = new PrintWriter(processingEnv.getFiler().createSourceFile("transformer.test.org.quiltmc.asmr.processor.verifier.VerifierTests").openWriter());
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}

			writer.write("package transformer.test.org.quiltmc.asmr.processor.verifier;class VerifierTests {");
		}
		for (Element annotated : roundEnvironment.getElementsAnnotatedWith(VerifierTests.Fails.class)) {
			TypeElement element = (TypeElement) annotated;
			writer.write("@org.junit.jupiter.api.Test void fails");
			writer.write(annotated.getSimpleName().toString());
			writer.write("() {" +
					" org.junit.jupiter.api.Assertions.assertFalse(org.quiltmc.asmr.processor.verifier.FridgeVerifier" +
					".verify(null, org.quiltmc.asmr.processor.test.AsmrClassTestUtil.findClassBytes(");
			writer.write(element.getQualifiedName().toString());
			writer.write(".class)));}");
		}

		for (Element annotated : roundEnvironment.getElementsAnnotatedWith(VerifierTests.Passes.class)) {
			TypeElement element = (TypeElement) annotated;
			writer.write("@org.junit.jupiter.api.Test void passes");
			writer.write(annotated.getSimpleName().toString());
			writer.write("() {" +
					" org.junit.jupiter.api.Assertions.assertTrue(org.quiltmc.asmr.processor.verifier.FridgeVerifier" +
					".verify(null, org.quiltmc.asmr.processor.test.AsmrClassTestUtil.findClassBytes(");
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
