package org.quiltmc.asmr.processor;

import org.quiltmc.asmr.processor.annotation.HideFromTransformers;

@HideFromTransformers
public interface AsmrPlatform {
    /**
     * Gets the (unmodified) bytecode of the given class, even if not readable or writable.
     */
    byte[] getClassBytecode(String internalName) throws ClassNotFoundException;
}
