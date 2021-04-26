package transformer.test.org.quiltmc.asmr.processor.verifier.blacklist.simple;

import org.quiltmc.asmr.processor.annotation.test.VerifierTests;
import org.quiltmc.asmr.processor.test.annotation.InternalMembers;

@VerifierTests.Fails
public class FieldBlacklist {
	void method() {
		InternalMembers.FIELD.getClass();
	}
}
