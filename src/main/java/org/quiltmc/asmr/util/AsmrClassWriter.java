package org.quiltmc.asmr.util;

import org.objectweb.asm.ClassWriter;
import org.quiltmc.asmr.processor.AsmrProcessor;

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
