package org.quiltmc.asmr.processor;

public class AsmrTestPlatform implements AsmrPlatform {
    @Override
    public byte[] getClassBytecode(String internalName) throws ClassNotFoundException {
        return AsmrClassTestUtil.findClassBytes(Class.forName(internalName.replace('/', '.')));
    }
}
