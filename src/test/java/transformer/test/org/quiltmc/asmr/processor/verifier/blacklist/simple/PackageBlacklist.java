package transformer.test.org.quiltmc.asmr.processor.verifier.blacklist.simple;

import org.quiltmc.asmr.processor.annotation.test.VerifierTests;
import org.quiltmc.asmr.processor.verifier.FridgeVerifier;

@VerifierTests.Fails
public class PackageBlacklist {
	void test() {
		FridgeVerifier.init(); // the verifier package is internal
	}
}
