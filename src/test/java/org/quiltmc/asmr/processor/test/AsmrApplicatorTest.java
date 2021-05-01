package org.quiltmc.asmr.processor.test;

import org.junit.jupiter.api.Test;
import org.quiltmc.asmr.processor.AsmrProcessor;
import org.quiltmc.asmr.processor.AsmrStandardPhases;
import org.quiltmc.asmr.processor.AsmrTransformer;
import org.quiltmc.asmr.processor.capture.AsmrNodeCapture;
import org.quiltmc.asmr.processor.capture.AsmrSliceCapture;
import org.quiltmc.asmr.processor.tree.member.AsmrMethodListNode;
import org.quiltmc.asmr.processor.tree.member.AsmrMethodNode;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AsmrApplicatorTest {
    @Test
    public void runTest() throws ReflectiveOperationException {
        AsmrTestPlatform platform = new AsmrTestPlatform();
        AsmrProcessor processor = new AsmrProcessor(platform);
        String targetClassName = TestTargetClass.class.getName().replace('.', '/');
        processor.addClass(targetClassName, platform.getClassBytecode(targetClassName));
        processor.addTransformer(TestTransformer.class);
        String sourceClassName = TestSourceClass.class.getName().replace('.', '/');
        processor.addClass(sourceClassName, platform.getClassBytecode(sourceClassName));
        processor.process();
        assertTrue(processor.getModifiedClassNames().contains(targetClassName));
        //noinspection ConstantConditions
        Class<?> transformedTestClass = AsmrClassTestUtil.defineClass(processor, processor.findClassImmediately(targetClassName));
        assertEquals("Hello Earth!", transformedTestClass.getMethod("getGreeting").invoke(null));
    }

    public static class TestTargetClass {

    }

    public static class TestSourceClass {
        public static String getGreeting() {
            return "Hello Earth!";
        }
        public static void foo() {}
    }

    public static class TestTransformer implements AsmrTransformer {
        @Override
        public List<String> getPhases() {
            return Collections.singletonList(AsmrStandardPhases.READ_INITIAL);
        }

        @Override
        public void addRoundDependencies(AsmrProcessor processor) {
        }

        @Override
        public void read(AsmrProcessor processor) {
            processor.withClass("org/quiltmc/asmr/processor/test/AsmrApplicatorTest$TestTargetClass", targetClass -> {
                AsmrSliceCapture<AsmrMethodNode> targetCapture = processor.refCapture(targetClass.methods(), 0, 0, true, false);
                processor.withClass("org/quiltmc/asmr/processor/test/AsmrApplicatorTest$TestSourceClass", sourceClass -> {
                    sourceClass.methods().forEach(sourceMethod -> {
                        if (!sourceMethod.name().value().equals("<init>")) {
                            AsmrNodeCapture<AsmrMethodNode> sourceCapture = processor.copyCapture(sourceMethod);
                            processor.addWrite(this, targetCapture, () -> {
                                AsmrMethodListNode methods = new AsmrMethodListNode();
                                processor.substitute(methods.add(), sourceCapture);
                                return methods;
                            });
                        }
                    });
                });
            });
        }
    }
}
