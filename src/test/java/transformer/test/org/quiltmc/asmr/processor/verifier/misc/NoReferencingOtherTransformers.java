package transformer.test.org.quiltmc.asmr.processor.verifier.misc;

import org.quiltmc.asmr.processor.AsmrProcessor;
import org.quiltmc.asmr.processor.AsmrTransformer;
import org.quiltmc.asmr.processor.annotation.test.VerifierTests;
import transformer.test.org.quiltmc.asmr.processor.verifier.blacklist.simple.ClassBlacklist;

import java.util.List;

@VerifierTests.Fails
public class NoReferencingOtherTransformers implements AsmrTransformer {
	@Override
	public List<String> getPhases() {
		return null;
	}

	@Override
	public void addDependencies(AsmrProcessor processor) {

	}

	@Override
	public void read(AsmrProcessor processor) {
		ClassBlacklist.class.getClass();
	}
}
