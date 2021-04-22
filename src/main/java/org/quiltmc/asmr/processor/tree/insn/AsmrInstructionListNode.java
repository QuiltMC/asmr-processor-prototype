package org.quiltmc.asmr.processor.tree.insn;

import org.quiltmc.asmr.processor.tree.AsmrNode;
import org.quiltmc.asmr.processor.tree.AsmrPolymorphicListNode;

import java.util.HashMap;
import java.util.Map;

public class AsmrInstructionListNode<E extends AsmrAbstractInsnNode<E>> extends AsmrPolymorphicListNode<E, AsmrInstructionListNode<E>> {
    private static final Map<Class<?>, Type<?>> TYPES = new HashMap<>();
    static {
        TYPES.put(AsmrNoOperandInsnNode.class, NoOperandInsnType.INSTANCE);
        TYPES.put(AsmrIntInsnNode.class, IntInsnType.INSTANCE);
        TYPES.put(AsmrVarInsnNode.class, VarInsnType.INSTANCE);
        TYPES.put(AsmrTypeInsnNode.class, TypeInsnType.INSTANCE);
        TYPES.put(AsmrFieldInsnNode.class, FieldInsnType.INSTANCE);
        TYPES.put(AsmrMethodInsnNode.class, MethodInsnType.INSTANCE);
        TYPES.put(AsmrInvokeDynamicInsnNode.class, InvokeDynamicInsnType.INSTANCE);
        TYPES.put(AsmrJumpInsnNode.class, JumpInsnType.INSTANCE);
        TYPES.put(AsmrLabelNode.class, LabelType.INSTANCE);
        TYPES.put(AsmrLdcInsnNode.class, LdcInsnType.INSTANCE);
        TYPES.put(AsmrIincInsnNode.class, IincInsnType.INSTANCE);
        TYPES.put(AsmrSwitchInsnNode.class, SwitchInsnType.INSTANCE);
        TYPES.put(AsmrMultiANewArrayInsnNode.class, MultiANewArrayInsnType.INSTANCE);
        TYPES.put(AsmrLineNumberNode.class, LineNumberType.INSTANCE);
    }

    public AsmrInstructionListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    protected AsmrInstructionListNode<E> newInstance(AsmrNode<?> parent) {
        return new AsmrInstructionListNode<>(parent);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected E newElement(Type<?> type) {
        if (type instanceof InstructionType) {
            return (E) ((InstructionType<?>) type).create(this);
        } else {
            throw new IllegalArgumentException("Cannot create instruction list element of type " + type);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Type<? extends E> getType(E element) {
        Type<? extends E> type = (Type<? extends E>) TYPES.get(element.getClass());
        if (type != null) {
            return type;
        } else {
            throw new IllegalArgumentException(element + " is not of an instruction list element type");
        }
    }

    @FunctionalInterface
    private interface ElementCreator { AsmrAbstractInsnNode<?> create(AsmrNode<?> parent); }
    public interface InstructionType<T> extends Type<T>, ElementCreator {}

    public enum NoOperandInsnType implements InstructionType<AsmrNoOperandInsnNode> {
        INSTANCE;
        @Override public AsmrAbstractInsnNode<?> create(AsmrNode<?> parent) { return new AsmrNoOperandInsnNode(parent); }
    }
    public enum IntInsnType implements InstructionType<AsmrIntInsnNode> {
        INSTANCE;
        @Override public AsmrAbstractInsnNode<?> create(AsmrNode<?> parent) { return new AsmrIntInsnNode(parent); }
    }
    public enum VarInsnType implements InstructionType<AsmrVarInsnNode> {
        INSTANCE;
        @Override public AsmrAbstractInsnNode<?> create(AsmrNode<?> parent) { return new AsmrVarInsnNode(parent); }
    }
    public enum TypeInsnType implements InstructionType<AsmrTypeInsnNode> {
        INSTANCE;
        @Override public AsmrAbstractInsnNode<?> create(AsmrNode<?> parent) { return new AsmrTypeInsnNode(parent); }
    }
    public enum FieldInsnType implements InstructionType<AsmrFieldInsnNode> {
        INSTANCE;
        @Override public AsmrAbstractInsnNode<?> create(AsmrNode<?> parent) { return new AsmrFieldInsnNode(parent); }
    }
    public enum MethodInsnType implements InstructionType<AsmrMethodInsnNode> {
        INSTANCE;
        @Override public AsmrAbstractInsnNode<?> create(AsmrNode<?> parent) { return new AsmrMethodInsnNode(parent); }
    }
    public enum InvokeDynamicInsnType implements InstructionType<AsmrInvokeDynamicInsnNode> {
        INSTANCE;
        @Override public AsmrAbstractInsnNode<?> create(AsmrNode<?> parent) { return new AsmrInvokeDynamicInsnNode(parent); }
    }
    public enum JumpInsnType implements InstructionType<AsmrJumpInsnNode> {
        INSTANCE;
        @Override public AsmrAbstractInsnNode<?> create(AsmrNode<?> parent) { return new AsmrJumpInsnNode(parent); }
    }
    public enum LabelType implements InstructionType<AsmrLabelNode> {
        INSTANCE;
        @Override public AsmrAbstractInsnNode<?> create(AsmrNode<?> parent) { return new AsmrLabelNode(parent); }
    }
    public enum LdcInsnType implements InstructionType<AsmrLdcInsnNode> {
        INSTANCE;
        @Override public AsmrAbstractInsnNode<?> create(AsmrNode<?> parent) { return new AsmrLdcInsnNode(parent); }
    }
    public enum IincInsnType implements InstructionType<AsmrIincInsnNode> {
        INSTANCE;
        @Override public AsmrAbstractInsnNode<?> create(AsmrNode<?> parent) { return new AsmrIincInsnNode(parent); }
    }
    public enum SwitchInsnType implements InstructionType<AsmrSwitchInsnNode> {
        INSTANCE;
        @Override public AsmrAbstractInsnNode<?> create(AsmrNode<?> parent) { return new AsmrSwitchInsnNode(parent); }
    }
    public enum MultiANewArrayInsnType implements InstructionType<AsmrMultiANewArrayInsnNode> {
        INSTANCE;
        @Override public AsmrAbstractInsnNode<?> create(AsmrNode<?> parent) { return new AsmrMultiANewArrayInsnNode(parent); }
    }
    public enum LineNumberType implements InstructionType<AsmrLineNumberNode> {
        INSTANCE;
        @Override public AsmrAbstractInsnNode<?> create(AsmrNode<?> parent) { return new AsmrLineNumberNode(parent); }
    }
}
