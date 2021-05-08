package transformer.test.org.quiltmc.asmr.processor.verifier.whitelist;

import org.quiltmc.asmr.processor.AsmrProcessor;
import org.quiltmc.asmr.processor.AsmrTransformer;
import org.quiltmc.asmr.processor.annotation.test.VerifierTests;
import org.quiltmc.asmr.tree.AsmrNode;

import java.util.List;

@VerifierTests.Passes
public class PackageStartWhitelist implements AsmrTransformer {
	public void method(AsmrNode in) {
		in.parent();
	}

	@Override
	public List<String> getPhases() {
		return null;
	}

	@Override
	public void addDependencies(AsmrProcessor processor) {

	}

	@Override
	public void read(AsmrProcessor processor) {

	}
}
