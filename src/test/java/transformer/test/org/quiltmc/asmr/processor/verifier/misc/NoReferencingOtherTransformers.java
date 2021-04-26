package transformer.test.org.quiltmc.asmr.processor.verifier.misc;

import org.quiltmc.asmr.processor.annotation.test.VerifierTests;
import transformer.test.org.quiltmc.asmr.processor.verifier.blacklist.simple.ClassBlacklist;

@VerifierTests.Fails
public class NoReferencingOtherTransformers {
	void method() {
		ClassBlacklist.class.getClass();
	}
}
