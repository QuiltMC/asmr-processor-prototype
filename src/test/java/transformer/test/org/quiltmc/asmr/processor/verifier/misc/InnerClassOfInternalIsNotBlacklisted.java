package transformer.test.org.quiltmc.asmr.processor.verifier.misc;

import org.quiltmc.asmr.processor.AsmrProcessor;
import org.quiltmc.asmr.processor.AsmrTransformer;
import org.quiltmc.asmr.processor.annotation.test.VerifierTests;
import org.quiltmc.asmr.processor.test.annotation.InternalClass;

import java.util.List;

@VerifierTests.Passes
public class InnerClassOfInternalIsNotBlacklisted implements AsmrTransformer {
	@Override
	public List<String> getPhases() {
		return null;
	}

	@Override
	public void addDependencies(AsmrProcessor processor) {

	}

	@Override
	public void read(AsmrProcessor processor) {
		InternalClass.NonInternalInner.SubSubClass.method();
	}
}
