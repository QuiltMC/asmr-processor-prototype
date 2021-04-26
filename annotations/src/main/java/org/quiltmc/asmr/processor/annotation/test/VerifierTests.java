package org.quiltmc.asmr.processor.annotation.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class VerifierTests {
	private VerifierTests() {}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.SOURCE)
	public @interface Passes {
	}
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.SOURCE)
	public @interface Fails {
	}
}
