package transformer.test.org.quiltmc.asmr.processor.verifier.misc;

import org.quiltmc.asmr.processor.annotation.test.VerifierTests;

import java.io.Serializable;

@VerifierTests.Fails
public class NoInterfaces implements Serializable {
}
