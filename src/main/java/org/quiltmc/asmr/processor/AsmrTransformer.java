package org.quiltmc.asmr.processor;

public interface AsmrTransformer {
    void apply(AsmrProcessor processor);
    void read(AsmrProcessor processor);
}
