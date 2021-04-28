package org.quiltmc.asmr.processor.test.annotation;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.VisibleForTesting;

@ApiStatus.Internal
@VisibleForTesting
public class InternalClass {
	public static void method() {
	}

	@ApiStatus.Internal
	public static class SuperInternalClass {
		@ApiStatus.Internal
		public static class EvenMoreInternalClass {
			public static void method() {

			}
		}

	}

	// It's intentional behavior to not have this apply to inner classes
	public static class NonInternalInner {
		public static class SubSubClass {
			public static void method() {

			}
		}
	}
}
