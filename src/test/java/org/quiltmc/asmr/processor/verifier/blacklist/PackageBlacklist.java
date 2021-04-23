package org.quiltmc.asmr.processor.verifier.blacklist;

import org.quiltmc.asmr.processor.verifier.FridgeVerifier;

public class PackageBlacklist {
	void test() {
		FridgeVerifier.init(); // the verifier package is internal
	}
}
