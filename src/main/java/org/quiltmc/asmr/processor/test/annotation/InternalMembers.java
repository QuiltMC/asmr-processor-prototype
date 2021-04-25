package org.quiltmc.asmr.processor.test.annotation;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.VisibleForTesting;

@VisibleForTesting
public class InternalMembers {
	@ApiStatus.Internal
	public static String FIELD = "";

	@ApiStatus.Internal
	public static void method() {
	}

	@ApiStatus.Internal
	public InternalMembers() {

	}
}
