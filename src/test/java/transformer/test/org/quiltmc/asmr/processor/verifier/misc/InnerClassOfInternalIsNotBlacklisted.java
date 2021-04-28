package transformer.test.org.quiltmc.asmr.processor.verifier.misc;

import org.quiltmc.asmr.processor.annotation.test.VerifierTests;
import org.quiltmc.asmr.processor.test.annotation.InternalClass;

@VerifierTests.Passes
public class InnerClassOfInternalIsNotBlacklisted {
	public static void method() {
		InternalClass.NonInternalInner.SubSubClass.method();
	}
}
