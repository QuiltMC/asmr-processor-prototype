package org.quiltmc.asmr.processor;

import org.objectweb.asm.ClassWriter;

public class AsmrClassWriter extends ClassWriter {
    private final AsmrProcessor processor;

    public AsmrClassWriter(AsmrProcessor processor) {
        super(COMPUTE_FRAMES);
        this.processor = processor;
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        return processor.getCommonSuperClass(type1, type2);
    }
}
