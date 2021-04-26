package transformer.test.org.quiltmc.asmr.processor.verifier.blacklist.simple;

import org.quiltmc.asmr.processor.annotation.test.VerifierTests;
import org.quiltmc.asmr.processor.test.annotation.InternalClass;

@VerifierTests.Fails
public class ClassBlacklist {
	void method() {
		InternalClass.method();
	}
}
