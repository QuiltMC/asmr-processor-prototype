package org.quiltmc.asmr.processor;

import org.junit.jupiter.api.Test;
import org.quiltmc.asmr.processor.capture.AsmrNodeCapture;
import org.quiltmc.asmr.processor.tree.AsmrValueNode;
import org.quiltmc.asmr.processor.tree.insn.AsmrAbstractInsnNode;
import org.quiltmc.asmr.processor.tree.insn.AsmrConstantList;
import org.quiltmc.asmr.processor.tree.insn.AsmrLdcInsnNode;
import org.quiltmc.asmr.processor.tree.member.AsmrMethodNode;

import static org.junit.jupiter.api.Assertions.*;

public class AsmrApplicatorTest {
    @Test
    public void runTest() throws ReflectiveOperationException {
        AsmrTestPlatform platform = new AsmrTestPlatform();
        AsmrProcessor processor = new AsmrProcessor(platform);
        String targetClassName = TestTargetClass.class.getName().replace('.', '/');
        processor.addClass(targetClassName, platform.getClassBytecode(targetClassName));
        processor.addTransformer(TestTransformer.class);
        processor.process();
        assertTrue(processor.getModifiedClassNames().contains(targetClassName));
        //noinspection ConstantConditions
        Class<?> transformedTestClass = AsmrClassTestUtil.defineClass(processor, processor.findClassImmediately(targetClassName));
        assertEquals("Hello Earth!", transformedTestClass.getMethod("getGreeting").invoke(null));
    }

    public static class TestTargetClass {
        public static String getGreeting() {
            return "Hello World!";
        }
    }

    public static class TestTransformer implements AsmrTransformer {
        @Override
        public void read(AsmrProcessor processor) {
            processor.withClass("org/quiltmc/asmr/processor/AsmrApplicatorTest$TestTargetClass", classNode -> {
                for (AsmrMethodNode method : classNode.methods()) {
                    if (method.name().value().equals("getGreeting")) {
                        for (AsmrAbstractInsnNode<?> insn : method.body().instructions()) {
                            if (insn instanceof AsmrLdcInsnNode) {
                                AsmrLdcInsnNode ldcNode = (AsmrLdcInsnNode) insn;
                                if (ldcNode.cstList().getType(0) == AsmrConstantList.StringType.INSTANCE) {
                                    AsmrValueNode<String> helloNode = ldcNode.cstList().get(0, AsmrConstantList.StringType.INSTANCE);
                                    if (helloNode.value().equals("Hello World!")) {
                                        AsmrNodeCapture<AsmrValueNode<String>> helloCapture = processor.refCapture(helloNode);
                                        processor.addWrite(this, helloCapture, () -> {
                                            return new AsmrValueNode<String>(null).init("Hello Earth!");
                                        });
                                    }
                                }
                            }
                        }
                    }
                }

            });
        }
    }
}
