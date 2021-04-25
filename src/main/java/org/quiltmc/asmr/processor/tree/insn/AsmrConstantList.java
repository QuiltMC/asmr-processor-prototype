package org.quiltmc.asmr.processor.tree.insn;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.processor.tree.AsmrNode;
import org.quiltmc.asmr.processor.tree.AsmrPolymorphicListNode;
import org.quiltmc.asmr.processor.tree.AsmrValueNode;

import java.util.HashMap;
import java.util.Map;

public class AsmrConstantList<E extends AsmrNode<E>> extends AsmrPolymorphicListNode<E, AsmrConstantList<E>> {
    private static final Map<Class<?>, Type<?>> VALUE_TYPES = new HashMap<>();
    static {
        VALUE_TYPES.put(Integer.class, IntType.INSTANCE);
        VALUE_TYPES.put(Float.class, FloatType.INSTANCE);
        VALUE_TYPES.put(Long.class, LongType.INSTANCE);
        VALUE_TYPES.put(Double.class, DoubleType.INSTANCE);
        VALUE_TYPES.put(String.class, StringType.INSTANCE);
        VALUE_TYPES.put(org.objectweb.asm.Type.class, ClassType.INSTANCE);
    }

    public AsmrConstantList(AsmrNode<?> parent) {
        super(parent);
    }

    @ApiStatus.Internal
    @Override
    public AsmrConstantList<E> newInstance(AsmrNode<?> parent) {
        return new AsmrConstantList<>(parent);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected E newElement(Type<?> type) {
        if (type == HandleType.INSTANCE) {
            return (E) new AsmrHandleNode(this);
        } else if (type == ConstantDynamicType.INSTANCE) {
            return (E) new AsmrConstantDynamicNode(this);
        } else if (type instanceof BsmArgumentListType) {
            return (E) new AsmrValueNode<>(this);
        } else {
            throw new IllegalArgumentException("Cannot create constant element of type " + type);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Type<? extends E> getType(E element) {
        if (element instanceof AsmrHandleNode) {
            return (Type<? extends E>) HandleType.INSTANCE;
        } else if (element instanceof AsmrConstantDynamicNode) {
            return (Type<? extends E>) ConstantDynamicType.INSTANCE;
        } else if (element instanceof AsmrValueNode) {
            Type<? extends E> type = (Type<? extends E>) VALUE_TYPES.get(((AsmrValueNode<?>) element).value().getClass());
            if (type != null) {
                return type;
            }
        }

        throw new IllegalArgumentException(element + " is not of a constant type");
    }

    public interface BsmArgumentListType<T> extends Type<T> {}
    public enum IntType implements BsmArgumentListType<AsmrValueNode<Integer>> { INSTANCE }
    public enum FloatType implements BsmArgumentListType<AsmrValueNode<Float>> { INSTANCE }
    public enum LongType implements BsmArgumentListType<AsmrValueNode<Long>> { INSTANCE }
    public enum DoubleType implements BsmArgumentListType<AsmrValueNode<Double>> { INSTANCE }
    public enum StringType implements BsmArgumentListType<AsmrValueNode<String>> { INSTANCE }
    public enum ClassType implements BsmArgumentListType<AsmrValueNode<org.objectweb.asm.Type>> { INSTANCE }
    public enum HandleType implements BsmArgumentListType<AsmrHandleNode> { INSTANCE }
    public enum ConstantDynamicType implements BsmArgumentListType<AsmrConstantDynamicNode> { INSTANCE }
}
