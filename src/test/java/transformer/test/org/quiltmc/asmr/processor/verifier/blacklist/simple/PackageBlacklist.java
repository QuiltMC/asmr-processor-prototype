package transformer.test.org.quiltmc.asmr.processor.verifier.blacklist.simple;

import org.quiltmc.asmr.processor.AsmrProcessor;
import org.quiltmc.asmr.processor.AsmrTransformer;
import org.quiltmc.asmr.processor.annotation.test.VerifierTests;
import org.quiltmc.asmr.processor.test.annotation.InternalClass;
import org.quiltmc.asmr.processor.verifier.FridgeVerifier;

import java.util.List;

@VerifierTests.Fails
public class PackageBlacklist implements AsmrTransformer {
	@Override
	public List<String> getPhases() {
		return null;
	}

	@Override
	public void addDependencies(AsmrProcessor processor) {
	}

	@Override
	public void read(AsmrProcessor processor) {
		FridgeVerifier.init(); // this package is internal
	}
}
