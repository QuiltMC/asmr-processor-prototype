package transformer.test.org.quiltmc.asmr.processor.verifier.whitelist;

import org.quiltmc.asmr.processor.annotation.test.VerifierTests;
import org.quiltmc.asmr.processor.tree.AsmrNode;

@VerifierTests.Passes
public class PackageStartWhitelist {
	public void method(AsmrNode in) {
		in.parent();
	}
}
