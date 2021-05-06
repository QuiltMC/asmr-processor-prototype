package org.quiltmc.asmr.processor.test;

import org.junit.jupiter.api.Assertions;
import org.quiltmc.asmr.processor.AsmrClassWriter;
import org.quiltmc.asmr.processor.AsmrProcessor;
import org.quiltmc.asmr.processor.AsmrTransformer;
import org.quiltmc.asmr.processor.tree.member.AsmrClassNode;
import org.quiltmc.asmr.processor.util.Pair;
import org.quiltmc.asmr.processor.verifier.FridgeVerifier;

import static org.junit.jupiter.api.Assertions.*;

public class TransformerTestProctor {
	@SafeVarargs
	public static Pair<AsmrProcessor, AsmrClassNode> test(Class<?> target, Class<?>[] source, Class<? extends AsmrTransformer>... transformers) {
		try {
			AsmrTestPlatform platform = new AsmrTestPlatform();
			String targetClassName = target.getName().replace('.', '/');
			byte[] bytecode = platform.getClassBytecode(targetClassName);
			byte[] firstRound;
			{
				AsmrProcessor processor = new AsmrProcessor(platform);


				FridgeVerifier.verify(platform, bytecode);

				processor.addClass(targetClassName, platform.getClassBytecode(targetClassName));
				for (Class<? extends AsmrTransformer> transformer : transformers) {
					processor.addTransformer(transformer);
				}

				for (Class<?> c : source) {
					String sourceClassName = c.getName().replace('.', '/');
					processor.addClass(sourceClassName, platform.getClassBytecode(sourceClassName));
				}

				processor.process();
				AsmrClassNode node = processor.findClassImmediately(targetClassName);
				AsmrClassWriter cw = new AsmrClassWriter(processor);
				node.accept(cw);
				firstRound = cw.toByteArray();
			}
			// Doing a second round is more to test that the processor has consistent output than the transformers,
			// but both is good.
			byte[] secondRound;
			AsmrProcessor processor;
			AsmrClassNode processed;
			// TODO: deduplicate
			{
				processor = new AsmrProcessor(platform);
				processor.addClass(targetClassName, platform.getClassBytecode(targetClassName));
				for (Class<? extends AsmrTransformer> transformer : transformers) {
					processor.addTransformer(transformer);
				}

				for (Class<?> c : source) {
					String sourceClassName = c.getName().replace('.', '/');
					processor.addClass(sourceClassName, platform.getClassBytecode(sourceClassName));
				}

				processor.process();
				AsmrClassNode node = processor.findClassImmediately(targetClassName);
				AsmrClassWriter cw = new AsmrClassWriter(processor);
				node.accept(cw);
				secondRound = cw.toByteArray();
				processed = node;
			}

			assertArrayEquals(firstRound, secondRound);
			return Pair.of(processor, processed);
		} catch (Exception ex) {
			Assertions.fail(ex);
			throw new AssertionError("unreachable");
		}
	}
}
