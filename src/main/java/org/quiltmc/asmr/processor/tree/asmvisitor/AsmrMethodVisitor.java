package org.quiltmc.asmr.processor.tree.asmvisitor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.quiltmc.asmr.processor.AsmrProcessor;
import org.quiltmc.asmr.processor.tree.AsmrTreeUtil;
import org.quiltmc.asmr.processor.tree.AsmrValueListNode;
import org.quiltmc.asmr.processor.tree.annotation.AsmrAnnotationListListNode;
import org.quiltmc.asmr.processor.tree.annotation.AsmrAnnotationNode;
import org.quiltmc.asmr.processor.tree.annotation.AsmrLocalVariableAnnotationNode;
import org.quiltmc.asmr.processor.tree.annotation.AsmrLocalVariableAnnotationTargetNode;
import org.quiltmc.asmr.processor.tree.annotation.AsmrTypeAnnotationNode;
import org.quiltmc.asmr.processor.tree.insn.AsmrAbstractInsnNode;
import org.quiltmc.asmr.processor.tree.insn.AsmrConstantDynamicNode;
import org.quiltmc.asmr.processor.tree.insn.AsmrConstantList;
import org.quiltmc.asmr.processor.tree.insn.AsmrFieldInsnNode;
import org.quiltmc.asmr.processor.tree.insn.AsmrHandleNode;
import org.quiltmc.asmr.processor.tree.insn.AsmrIincInsnNode;
import org.quiltmc.asmr.processor.tree.insn.AsmrInsnNode;
import org.quiltmc.asmr.processor.tree.insn.AsmrInstructionList;
import org.quiltmc.asmr.processor.tree.insn.AsmrIntInsnNode;
import org.quiltmc.asmr.processor.tree.insn.AsmrInvokeDynamicInsnNode;
import org.quiltmc.asmr.processor.tree.insn.AsmrJumpInsnNode;
import org.quiltmc.asmr.processor.tree.insn.AsmrLabelNode;
import org.quiltmc.asmr.processor.tree.insn.AsmrLdcInsnNode;
import org.quiltmc.asmr.processor.tree.insn.AsmrLineNumberNode;
import org.quiltmc.asmr.processor.tree.insn.AsmrMethodInsnNode;
import org.quiltmc.asmr.processor.tree.insn.AsmrMultiANewArrayInsnNode;
import org.quiltmc.asmr.processor.tree.insn.AsmrNoOperandInsnNode;
import org.quiltmc.asmr.processor.tree.insn.AsmrSwitchInsnNode;
import org.quiltmc.asmr.processor.tree.insn.AsmrSwitchKeyLabelNode;
import org.quiltmc.asmr.processor.tree.insn.AsmrTypeInsnNode;
import org.quiltmc.asmr.processor.tree.insn.AsmrVarInsnNode;
import org.quiltmc.asmr.processor.tree.member.AsmrMethodNode;
import org.quiltmc.asmr.processor.tree.method.AsmrIndex;
import org.quiltmc.asmr.processor.tree.method.AsmrLocalVariableNode;
import org.quiltmc.asmr.processor.tree.method.AsmrParameterNode;
import org.quiltmc.asmr.processor.tree.method.AsmrTryCatchBlockNode;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class AsmrMethodVisitor extends MethodVisitor {
    private final AsmrMethodNode methodNode;
    private final TreeMap<Integer, AsmrIndex> localVariableIndexes = new TreeMap<>();
    private final Map<Label, AsmrIndex> labelIndexes = new HashMap<>();

    private boolean visitedVisibleAnnotableParameterCount = false;
    private boolean visitedInvisibleAnnotableParameterCount = false;

    public AsmrMethodVisitor(AsmrMethodNode methodNode) {
        super(AsmrProcessor.ASM_VERSION);
        this.methodNode = methodNode;
    }

    // METHOD METADATA

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        AsmrAnnotationNode annotation = visible ? methodNode.visibleAnnotations().add() : methodNode.invisibleAnnotations().add();
        annotation.desc().init(descriptor);
        return new AsmrAnnotationVisitor(annotation);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        AsmrTypeAnnotationNode annotation = visible ? methodNode.visibleTypeAnnotations().add() : methodNode.invisibleTypeAnnotations().add();
        annotation.typeRef().init(typeRef);
        annotation.typePath().init(typePath == null ? "" : typePath.toString());
        annotation.desc().init(descriptor);
        return new AsmrAnnotationVisitor(annotation);
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        return new AsmrAnnotationVisitor.ArrayVisitor<>(methodNode.annotationDefault());
    }

    @Override
    public void visitParameter(String name, int access) {
        AsmrParameterNode parameter = methodNode.parameters().add();
        parameter.name().init(AsmrTreeUtil.fromNullableString(name));
        AsmrTreeUtil.flagsToModifierList(access, parameter.modifiers());
    }

    @Override
    public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
        if (visible) {
            visitedVisibleAnnotableParameterCount = true;
            methodNode.visibleAnnotableParameterCount().init(parameterCount);
        } else {
            visitedInvisibleAnnotableParameterCount = true;
            methodNode.invisibleAnnotableParameterCount().init(parameterCount);
        }
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        AsmrAnnotationListListNode parameterAnnotations = visible ? methodNode.visibleParameterAnnotations() : methodNode.invisibleParameterAnnotations();
        if (parameterAnnotations.isEmpty()) {
            for (int i = 0, e = Type.getArgumentTypes(methodNode.desc().value()).length; i < e; i++) {
                parameterAnnotations.add();
            }
        }
        AsmrAnnotationNode annotation = parameterAnnotations.get(parameter).add();
        annotation.desc().init(descriptor);
        return new AsmrAnnotationVisitor(annotation);
    }

    // METHOD BODY

    @Override
    public void visitInsn(int opcode) {
        AsmrNoOperandInsnNode insn = methodNode.body().instructions().add(AsmrInstructionList.NoOperandInsnType.INSTANCE);
        insn.opcode().init(opcode);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        AsmrIntInsnNode insn = methodNode.body().instructions().add(AsmrInstructionList.IntInsnType.INSTANCE);
        insn.opcode().init(opcode);
        insn.operand().init(operand);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        AsmrVarInsnNode insn = methodNode.body().instructions().add(AsmrInstructionList.VarInsnType.INSTANCE);
        insn.opcode().init(opcode);
        insn.varIndex().init(getLocalVariableIndex(var));
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        AsmrTypeInsnNode insn = methodNode.body().instructions().add(AsmrInstructionList.TypeInsnType.INSTANCE);
        insn.opcode().init(opcode);
        insn.desc().init(type);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        AsmrFieldInsnNode insn = methodNode.body().instructions().add(AsmrInstructionList.FieldInsnType.INSTANCE);
        insn.opcode().init(opcode);
        insn.owner().init(owner);
        insn.name().init(name);
        insn.desc().init(descriptor);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        AsmrMethodInsnNode insn = methodNode.body().instructions().add(AsmrInstructionList.MethodInsnType.INSTANCE);
        insn.opcode().init(opcode);
        insn.owner().init(owner);
        insn.name().init(name);
        insn.desc().init(descriptor);
        insn.itf().init(isInterface);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        AsmrInvokeDynamicInsnNode insn = methodNode.body().instructions().add(AsmrInstructionList.InvokeDynamicInsnType.INSTANCE);
        insn.opcode().init(Opcodes.INVOKEDYNAMIC);
        insn.name().init(name);
        insn.desc().init(descriptor);
        transferHandle(insn.bsm(), bootstrapMethodHandle);
        transferConstants(insn.bsmArgs(), bootstrapMethodArguments);
    }

    private static void transferHandle(AsmrHandleNode destHandle, Handle handle) {
        destHandle.tag().init(AsmrHandleNode.Tag.byValue(handle.getTag()));
        destHandle.owner().init(handle.getOwner());
        destHandle.name().init(handle.getName());
        destHandle.desc().init(handle.getDesc());
        destHandle.itf().init(handle.isInterface());
    }

    private static void transferConstants(AsmrConstantList<?> destBsmArgs, Object... bsmArgs) {
        if (bsmArgs != null) {
            for (Object arg : bsmArgs) {
                if (arg instanceof Handle) {
                    AsmrHandleNode destHandle = destBsmArgs.add(AsmrConstantList.HandleType.INSTANCE);
                    transferHandle(destHandle, (Handle) arg);
                } else if (arg instanceof ConstantDynamic) {
                    ConstantDynamic constantDynamic = (ConstantDynamic) arg;
                    AsmrConstantDynamicNode destConstantDynamic = destBsmArgs.add(AsmrConstantList.ConstantDynamicType.INSTANCE);
                    destConstantDynamic.name().init(constantDynamic.getName());
                    destConstantDynamic.desc().init(constantDynamic.getDescriptor());
                    transferHandle(destConstantDynamic.bsm(), constantDynamic.getBootstrapMethod());
                    Object[] subBsmArgs = new Object[constantDynamic.getBootstrapMethodArgumentCount()];
                    for (int i = 0; i < subBsmArgs.length; i++) {
                        subBsmArgs[i] = constantDynamic.getBootstrapMethodArgument(i);
                    }
                    transferConstants(destConstantDynamic.bsmArgs(), subBsmArgs);
                } else if (arg instanceof Integer) {
                    destBsmArgs.add(AsmrConstantList.IntType.INSTANCE).init((Integer) arg);
                } else if (arg instanceof Float) {
                    destBsmArgs.add(AsmrConstantList.FloatType.INSTANCE).init((Float) arg);
                } else if (arg instanceof Long) {
                    destBsmArgs.add(AsmrConstantList.LongType.INSTANCE).init((Long) arg);
                } else if (arg instanceof Double) {
                    destBsmArgs.add(AsmrConstantList.DoubleType.INSTANCE).init((Double) arg);
                } else if (arg instanceof String) {
                    destBsmArgs.add(AsmrConstantList.StringType.INSTANCE).init((String) arg);
                } else if (arg instanceof org.objectweb.asm.Type) {
                    destBsmArgs.add(AsmrConstantList.ClassType.INSTANCE).init((org.objectweb.asm.Type) arg);
                } else {
                    throw new IllegalArgumentException("Unrecognized BSM constant argument type: " + arg.getClass().getName());
                }
            }
        }
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        AsmrJumpInsnNode insn = methodNode.body().instructions().add(AsmrInstructionList.JumpInsnType.INSTANCE);
        insn.opcode().init(opcode);
        insn.label().init(getLabelIndex(label));
    }

    @Override
    public void visitLabel(Label label) {
        AsmrLabelNode labelNode = methodNode.body().instructions().add(AsmrInstructionList.LabelType.INSTANCE);
        labelNode.label().init(getLabelIndex(label));
    }

    @Override
    public void visitLdcInsn(Object value) {
        AsmrLdcInsnNode insn = methodNode.body().instructions().add(AsmrInstructionList.LdcInsnType.INSTANCE);
        insn.opcode().init(Opcodes.LDC);
        transferConstants(insn.cstList(), value);
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        AsmrIincInsnNode insn = methodNode.body().instructions().add(AsmrInstructionList.IincInsnType.INSTANCE);
        insn.opcode().init(Opcodes.IINC);
        insn.var().init(getLocalVariableIndex(var));
        insn.incr().init(increment);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        AsmrSwitchInsnNode insn = methodNode.body().instructions().add(AsmrInstructionList.SwitchInsnType.INSTANCE);
        insn.opcode().init(Opcodes.TABLESWITCH);
        insn.dflt().init(getLabelIndex(dflt));
        for (int i = 0; i < labels.length; i++) {
            if (labels[i] != dflt) {
                AsmrSwitchKeyLabelNode keyLabel = insn.labels().add();
                keyLabel.key().init(min + i);
                keyLabel.label().init(getLabelIndex(labels[i]));
            }
        }
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        AsmrSwitchInsnNode insn = methodNode.body().instructions().add(AsmrInstructionList.SwitchInsnType.INSTANCE);
        insn.opcode().init(Opcodes.LOOKUPSWITCH);
        insn.dflt().init(getLabelIndex(dflt));
        for (int i = 0; i < keys.length; i++) {
            AsmrSwitchKeyLabelNode keyLabel = insn.labels().add();
            keyLabel.key().init(keys[i]);
            keyLabel.label().init(getLabelIndex(labels[i]));
        }
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        AsmrMultiANewArrayInsnNode insn = methodNode.body().instructions().add(AsmrInstructionList.MultiANewArrayInsnType.INSTANCE);
        insn.opcode().init(Opcodes.MULTIANEWARRAY);
        insn.desc().init(descriptor);
        insn.dims().init(numDimensions);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        AsmrLineNumberNode lineNumberNode = methodNode.body().instructions().add(AsmrInstructionList.LineNumberType.INSTANCE);
        lineNumberNode.line().init(line);
        lineNumberNode.start().init(getLabelIndex(start));
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        if (methodNode.body().instructions().isEmpty()) {
            throw new IllegalStateException("Called visitInsnAnnotation before any instructions");
        }
        AsmrAbstractInsnNode<?> abstractInsn = methodNode.body().instructions().get(methodNode.body().instructions().size() - 1);
        if (!(abstractInsn instanceof AsmrInsnNode)) {
            throw new IllegalStateException("Called visitInsnAnnotation after an AsmrAbstractInsnNode that's not a AsmrInsnNode");
        }
        AsmrInsnNode<?> insn = (AsmrInsnNode<?>) abstractInsn;

        AsmrTypeAnnotationNode annotation = visible ? insn.visibleTypeAnnotations().add() : insn.invisibleTypeAnnotations().add();
        annotation.typeRef().init(typeRef);
        annotation.typePath().init(typePath == null ? "" : typePath.toString());
        annotation.desc().init(descriptor);
        return new AsmrAnnotationVisitor(annotation);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        AsmrTryCatchBlockNode tryCatchBlock = methodNode.body().tryCatchBlocks().add();
        tryCatchBlock.start().init(getLabelIndex(start));
        tryCatchBlock.end().init(getLabelIndex(end));
        tryCatchBlock.handler().init(getLabelIndex(handler));
        tryCatchBlock.type().init(AsmrTreeUtil.fromNullableString(type));
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        if (methodNode.body().tryCatchBlocks().isEmpty()) {
            throw new IllegalStateException("Called visitTryCatchAnnotation before any try catch blocks");
        }
        AsmrTryCatchBlockNode tryCatchBlock = methodNode.body().tryCatchBlocks().get(methodNode.body().tryCatchBlocks().size() - 1);
        AsmrTypeAnnotationNode annotation = visible ? tryCatchBlock.visibleTypeAnnotations().add() : tryCatchBlock.invisibleTypeAnnotations().add();

        annotation.typeRef().init(typeRef);
        annotation.typePath().init(typePath == null ? "" : typePath.toString());
        annotation.desc().init(descriptor);
        return new AsmrAnnotationVisitor(annotation);
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        AsmrLocalVariableNode localVariable = methodNode.body().localVariables().add();
        localVariable.name().init(name);
        localVariable.desc().init(descriptor);
        localVariable.signature().init(AsmrTreeUtil.fromNullableString(signature));
        localVariable.start().init(getLabelIndex(start));
        localVariable.end().init(getLabelIndex(end));
        localVariable.index().init(getLocalVariableIndex(index));
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor, boolean visible) {
        AsmrLocalVariableAnnotationNode annotation = visible ? methodNode.body().visibleLocalVariableAnnotations().add() : methodNode.body().invisibleLocalVariableAnnotations().add();
        annotation.typeRef().init(typeRef);
        annotation.typePath().init(typePath == null ? "" : typePath.toString());
        annotation.desc().init(descriptor);
        for (int i = 0; i < start.length; i++) {
            AsmrLocalVariableAnnotationTargetNode target = annotation.targets().add();
            target.start().init(getLabelIndex(start[i]));
            target.end().init(getLabelIndex(end[i]));
            target.index().init(getLocalVariableIndex(index[i]));
        }
        return new AsmrAnnotationVisitor(annotation);
    }

    // END

    @Override
    public void visitEnd() {
        if (!visitedVisibleAnnotableParameterCount) {
            methodNode.visibleAnnotableParameterCount().init(0);
        }
        if (!visitedInvisibleAnnotableParameterCount) {
            methodNode.invisibleAnnotableParameterCount().init(0);
        }

        localVariableIndexes.forEach((id, index) -> {
            AsmrValueListNode<AsmrIndex> localIndexes = methodNode.body().localIndexes();
            while (localIndexes.size() < id) {
                localIndexes.add().init(new AsmrIndex());
            }
            localIndexes.add().init(index);
        });
    }

    private AsmrIndex getLocalVariableIndex(int var) {
        return localVariableIndexes.computeIfAbsent(var, k -> new AsmrIndex());
    }

    private AsmrIndex getLabelIndex(Label label) {
        return labelIndexes.computeIfAbsent(label, k -> new AsmrIndex());
    }
}
