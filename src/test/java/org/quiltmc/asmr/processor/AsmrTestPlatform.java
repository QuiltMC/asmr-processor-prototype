package org.quiltmc.asmr.processor;

import java.io.ByteArrayOutputStream;

public class AsmrTestPlatform implements AsmrPlatform {
    @Override
    public byte[] getClassBytecode(String internalName) throws ClassNotFoundException {
        byte[][] bytecode = new byte[1][];
        AsmrClassTestUtil.findClass(Class.forName(internalName.replace('/', '.')), inputStream -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int n;
            while ((n = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, n);
            }
            bytecode[0] = baos.toByteArray();
        });
        return bytecode[0];
    }
}
