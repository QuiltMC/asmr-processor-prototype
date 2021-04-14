package org.quiltmc.asmr.processor;

public interface AsmrPlatform {
    /**
     * Gets the (unmodified) bytecode of the given class, even if not readable or writable.
     */
    byte[] getClassBytecode(String internalName) throws ClassNotFoundException;
}
