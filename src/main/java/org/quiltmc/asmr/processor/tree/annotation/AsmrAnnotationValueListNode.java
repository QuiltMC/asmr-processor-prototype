package org.quiltmc.asmr.processor.tree.annotation;

import org.quiltmc.asmr.processor.tree.AsmrNode;
import org.quiltmc.asmr.processor.tree.AsmrPolymorphicListNode;
import org.quiltmc.asmr.processor.tree.AsmrValueNode;

import java.util.HashMap;
import java.util.Map;

public class AsmrAnnotationValueListNode<T extends AsmrNode<T>> extends AsmrPolymorphicListNode<T, AsmrAnnotationValueListNode<T>> {
    private static final Map<Class<?>, Type<?>> VALUE_TYPE_TO_TYPE = new HashMap<>();
    static {
        VALUE_TYPE_TO_TYPE.put(Byte.class, ByteType.INSTANCE);
        VALUE_TYPE_TO_TYPE.put(Boolean.class, BooleanType.INSTANCE);
        VALUE_TYPE_TO_TYPE.put(Character.class, CharType.INSTANCE);
        VALUE_TYPE_TO_TYPE.put(Short.class, ShortType.INSTANCE);
        VALUE_TYPE_TO_TYPE.put(Integer.class, IntType.INSTANCE);
        VALUE_TYPE_TO_TYPE.put(Long.class, LongType.INSTANCE);
        VALUE_TYPE_TO_TYPE.put(Float.class, FloatType.INSTANCE);
        VALUE_TYPE_TO_TYPE.put(Double.class, DoubleType.INSTANCE);
        VALUE_TYPE_TO_TYPE.put(String.class, StringType.INSTANCE);
        VALUE_TYPE_TO_TYPE.put(org.objectweb.asm.Type.class, ClassType.INSTANCE);
    }

    public AsmrAnnotationValueListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    protected AsmrAnnotationValueListNode<T> newInstance(AsmrNode<?> parent) {
        return new AsmrAnnotationValueListNode<>(parent);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected T newElement(Type<?> type) {
        if (type == EnumReferenceType.INSTANCE) {
            return (T) new AsmrEnumReferenceNode(this);
        } else if (type == AnnotationType.INSTANCE) {
            return (T) new AsmrAnnotationNode(this);
        } else if (type == ArrayType.INSTANCE) {
            return (T) new AsmrAnnotationValueListNode<>(this);
        } else if (type instanceof AnnotationValueListType) {
            return (T) new AsmrValueNode<>(this);
        } else {
            throw new IllegalArgumentException("Cannot create annotation element of type " + type);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Type<? extends T> getType(T element) {
        if (element instanceof AsmrEnumReferenceNode) {
            return (Type<? extends T>) EnumReferenceType.INSTANCE;
        } else if (element instanceof AsmrAnnotationNode) {
            return (Type<? extends T>) AnnotationType.INSTANCE;
        } else if (element instanceof AsmrAnnotationValueListNode) {
            return (Type<? extends T>) ArrayType.INSTANCE;
        } else if (element instanceof AsmrValueNode) {
            Class<?> valueType = ((AsmrValueNode<?>) element).value().getClass();
            Type<? extends T> type = (Type<? extends T>) VALUE_TYPE_TO_TYPE.get(valueType);
            if (type != null) {
                return type;
            }
        }

        throw new IllegalArgumentException(element + " is not of an annotation value list type");
    }

    public interface AnnotationValueListType<T> extends Type<T> {}
    public enum ByteType implements AnnotationValueListType<AsmrValueNode<Byte>> { INSTANCE }
    public enum BooleanType implements AnnotationValueListType<AsmrValueNode<Boolean>> { INSTANCE }
    public enum CharType implements AnnotationValueListType<AsmrValueNode<Character>> { INSTANCE }
    public enum ShortType implements AnnotationValueListType<AsmrValueNode<Short>> { INSTANCE }
    public enum IntType implements AnnotationValueListType<AsmrValueNode<Integer>> { INSTANCE }
    public enum LongType implements AnnotationValueListType<AsmrValueNode<Long>> { INSTANCE }
    public enum FloatType implements AnnotationValueListType<AsmrValueNode<Float>> { INSTANCE }
    public enum DoubleType implements AnnotationValueListType<AsmrValueNode<Double>> { INSTANCE }
    public enum StringType implements AnnotationValueListType<AsmrValueNode<String>> { INSTANCE }
    public enum ClassType implements AnnotationValueListType<AsmrValueNode<org.objectweb.asm.Type>> { INSTANCE }
    public enum EnumReferenceType implements AnnotationValueListType<AsmrEnumReferenceNode> { INSTANCE }
    public enum AnnotationType implements AnnotationValueListType<AsmrAnnotationNode> { INSTANCE }
    public static class ArrayType<T extends AsmrNode<T>> implements AnnotationValueListType<AsmrAnnotationValueListNode<T>> {
        private static final ArrayType<?> INSTANCE = new ArrayType<>();
        @SuppressWarnings("unchecked")
        public static <T extends AsmrNode<T>> ArrayType<T> instance() {
            return (ArrayType<T>) INSTANCE;
        }
        private ArrayType() {}
    }
}
