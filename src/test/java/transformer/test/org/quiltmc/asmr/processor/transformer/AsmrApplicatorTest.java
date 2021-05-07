package transformer.test.org.quiltmc.asmr.processor.transformer;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.quiltmc.asmr.processor.AsmrProcessor;
import org.quiltmc.asmr.processor.AsmrStandardPhases;
import org.quiltmc.asmr.processor.AsmrTransformer;
import org.quiltmc.asmr.processor.capture.AsmrNodeCapture;
import org.quiltmc.asmr.processor.capture.AsmrSliceCapture;
import org.quiltmc.asmr.processor.test.AsmrClassTestUtil;
import org.quiltmc.asmr.processor.test.TransformerTestProctor;
import org.quiltmc.asmr.processor.tree.insn.AsmrAbstractInsnNode;
import org.quiltmc.asmr.processor.tree.insn.AsmrConstantList;
import org.quiltmc.asmr.processor.tree.insn.AsmrInstructionListNode;
import org.quiltmc.asmr.processor.tree.insn.AsmrLdcInsnNode;
import org.quiltmc.asmr.processor.tree.insn.AsmrMethodInsnNode;
import org.quiltmc.asmr.processor.tree.member.AsmrClassNode;
import org.quiltmc.asmr.processor.tree.member.AsmrMethodListNode;
import org.quiltmc.asmr.processor.tree.member.AsmrMethodNode;
import org.quiltmc.asmr.processor.util.Pair;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AsmrApplicatorTest {
    @Test
    public void runTest() throws ReflectiveOperationException {
        Pair<AsmrProcessor, AsmrClassNode> result = TransformerTestProctor.test(TestTargetClass.class,
                new Class[]{TestSourceClass.class}, TestTransformer.class);
        Class<?> transformedTestClass = AsmrClassTestUtil.defineClass(result.k, result.v);
        assertEquals("Hello Earth!", transformedTestClass.getMethod("getGreeting").invoke(null));
    }

    @Test
    public void testTransformerOrder() throws ReflectiveOperationException {
        Pair<AsmrProcessor, AsmrClassNode> result = TransformerTestProctor.test(OrderingTestTarget.class, new Class[0],
                WriteFirstTransformer.class, AWriteSecondTransformer.class);
        Class<?> transformedTestClass = AsmrClassTestUtil.defineClass(result.k, result.v);
        // if the second one injects at head afterwards, it adds to the string first
        assertEquals("secondfirst", transformedTestClass.getMethod("compute").invoke(null));
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
        public void addDependencies(AsmrProcessor processor) {

        }

        @Override
        public void read(AsmrProcessor processor) {
            /*
             * Copies all methods except "<init>" from all classes that contain a method "String getGreeting()" which load the string
             * constant "Hello Earth!", into the target class "TestTargetClass"
             */
            processor.withClass("transformer/test/org/quiltmc/asmr/processor/transformer/AsmrApplicatorTest$TestTargetClass", targetClass -> {
                AsmrSliceCapture<AsmrMethodNode> targetCapture = processor.refCapture(targetClass.methods(), 0, 0);
                processor.withClasses(name -> true, cp -> cp.hasString("Hello Earth!") && cp.hasUtf("getGreeting"), sourceClass -> {
                    AsmrMethodNode getGreeting = sourceClass.methods().findMethod("getGreeting", "()Ljava/lang/String;");
                    if (getGreeting == null) {
                        return; // next class
                    }
                    boolean hasHelloEarth = false;
                    for (AsmrAbstractInsnNode<?> insn : getGreeting.body().instructions()) {
                        if (insn instanceof AsmrLdcInsnNode) {
                            AsmrLdcInsnNode ldcInsn = (AsmrLdcInsnNode) insn;
                            if (ldcInsn.cstList().getType(0) == AsmrConstantList.StringType.INSTANCE
                                    && ldcInsn.cstList().get(0, AsmrConstantList.StringType.INSTANCE).value().equals("Hello Earth!")) {
                                hasHelloEarth = true;
                            }
                        }
                    }
                    if (hasHelloEarth) {
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
                    }
                });
            });
        }
    }

    public static class OrderingTestTarget {
        public static String str = "";
        public static void append(String a) {
            str += a;
        }
        public static String compute() {
            return str;
        }
    }

    public static class WriteFirstTransformer implements AsmrTransformer {
        @Override
        public List<String> getPhases() {
            return Collections.singletonList(AsmrStandardPhases.READ_INITIAL);
        }

        @Override
        public void addDependencies(AsmrProcessor processor) {
        }

        @Override
        public void read(AsmrProcessor processor) {
            doRead(processor);
        }

        @SuppressWarnings("unchecked")
        private <N extends AsmrAbstractInsnNode<N>> void doRead(AsmrProcessor processor) {
            processor.withClass("transformer/test/org/quiltmc/asmr/processor/transformer/AsmrApplicatorTest$OrderingTestTarget", classNode -> {
                AsmrMethodNode method = classNode.methods().findMethod("compute", "()Ljava/lang/String;");
                if (method == null) throw new NullPointerException();
                AsmrSliceCapture<N> targetCapture = (AsmrSliceCapture<N>) processor.refCapture(method.body().instructions(), 0, 0);
                processor.addWrite(this, targetCapture, () -> {
                    AsmrInstructionListNode<N> toInsert = new AsmrInstructionListNode<>();
                    AsmrLdcInsnNode ldcInsn = toInsert.add(AsmrInstructionListNode.LdcInsnType.INSTANCE);
                    ldcInsn.opcode().init(Opcodes.LDC);
                    ldcInsn.cstList().add(AsmrConstantList.StringType.INSTANCE).init("first");
                    AsmrMethodInsnNode methodInsn = toInsert.add(AsmrInstructionListNode.MethodInsnType.INSTANCE);
                    methodInsn.opcode().init(Opcodes.INVOKESTATIC);
                    methodInsn.owner().init("transformer/test/org/quiltmc/asmr/processor/transformer/AsmrApplicatorTest$OrderingTestTarget");
                    methodInsn.name().init("append");
                    methodInsn.desc().init("(Ljava/lang/String;)V");
                    methodInsn.itf().init(false);
                    return toInsert;
                });
            });
        }
    }

    // give this transformer a name which is lexicographically before the other transformer to make sure the dependency works correctly
    public static class AWriteSecondTransformer implements AsmrTransformer {
        @Override
        public List<String> getPhases() {
            return Collections.singletonList(AsmrStandardPhases.READ_INITIAL);
        }

        @Override
        public void addDependencies(AsmrProcessor processor) {
            processor.addWriteDependency(this, "transformer.test.org.quiltmc.asmr.processor.transformer.AsmrApplicatorTest$WriteFirstTransformer");
        }

        @Override
        public void read(AsmrProcessor processor) {
            doRead(processor);
        }

        @SuppressWarnings("unchecked")
        private <N extends AsmrAbstractInsnNode<N>> void doRead(AsmrProcessor processor) {
            processor.withClass("transformer/test/org/quiltmc/asmr/processor/transformer/AsmrApplicatorTest$OrderingTestTarget", classNode -> {
                AsmrMethodNode method = classNode.methods().findMethod("compute", "()Ljava/lang/String;");
                if (method == null) throw new NullPointerException();
                AsmrSliceCapture<N> targetCapture = (AsmrSliceCapture<N>) processor.refCapture(method.body().instructions(), 0, 0);
                processor.addWrite(this, targetCapture, () -> {
                    AsmrInstructionListNode<N> toInsert = new AsmrInstructionListNode<>();
                    AsmrLdcInsnNode ldcInsn = toInsert.add(AsmrInstructionListNode.LdcInsnType.INSTANCE);
                    ldcInsn.opcode().init(Opcodes.LDC);
                    ldcInsn.cstList().add(AsmrConstantList.StringType.INSTANCE).init("second");
                    AsmrMethodInsnNode methodInsn = toInsert.add(AsmrInstructionListNode.MethodInsnType.INSTANCE);
                    methodInsn.opcode().init(Opcodes.INVOKESTATIC);
                    methodInsn.owner().init("transformer/test/org/quiltmc/asmr/processor/transformer/AsmrApplicatorTest$OrderingTestTarget");
                    methodInsn.name().init("append");
                    methodInsn.desc().init("(Ljava/lang/String;)V");
                    methodInsn.itf().init(false);
                    return toInsert;
                });
            });
        }
    }
}
