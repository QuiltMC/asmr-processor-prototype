package org.quiltmc.asmr.processor.verifier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.quiltmc.asmr.processor.AsmrClassTestUtil;
import org.quiltmc.asmr.processor.verifier.blacklist.ClassBlacklist;
import org.quiltmc.asmr.processor.verifier.blacklist.FieldBlacklist;
import org.quiltmc.asmr.processor.verifier.blacklist.MethodBlacklist;
import org.quiltmc.asmr.processor.verifier.blacklist.PackageBlacklist;

public class VerifierTests {
	@Test
	void blacklistPackage() {
		fail(PackageBlacklist.class);
	}

	@Test
	void blacklistClass() {
		fail(ClassBlacklist.class);
	}

	@Test
	void blacklistField() {
		fail(FieldBlacklist.class);
	}

	@Test
	void blacklistMethod() {
		fail(MethodBlacklist.class);
	}

	private static void fail(Class<?> clazz) {
		Assertions.assertFalse(FridgeVerifier.verify(AsmrClassTestUtil.findClassBytes(clazz)));
	}
}
