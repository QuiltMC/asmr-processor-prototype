package transformer.test.org.quiltmc.asmr.processor.verifier.blacklist.simple;

import org.quiltmc.asmr.processor.AsmrProcessor;
import org.quiltmc.asmr.processor.AsmrTransformer;
import org.quiltmc.asmr.processor.annotation.test.VerifierTests;
import org.quiltmc.asmr.processor.test.annotation.InternalClass;
import org.quiltmc.asmr.processor.test.annotation.InternalMembers;

import java.util.List;

@VerifierTests.Fails
public class FieldBlacklist  implements AsmrTransformer {
	@Override
	public List<String> getPhases() {
		return null;
	}

	@Override
	public void addDependencies(AsmrProcessor processor) {
	}

	@Override
	public void read(AsmrProcessor processor) {
		InternalMembers.FIELD.getClass();
	}
}
