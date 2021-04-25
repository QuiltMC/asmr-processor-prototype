package org.quiltmc.asmr.processor.tree.annotation;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.processor.tree.AsmrNode;
import org.quiltmc.asmr.processor.tree.AsmrPolymorphicListNode;

import java.util.HashMap;
import java.util.Map;

public class AsmrAnnotationNamedListNode<V extends AsmrNode<V>, E extends AsmrNamedNode<V, E>>
        extends AsmrPolymorphicListNode<E, AsmrAnnotationNamedListNode<V, E>> {
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

    public AsmrAnnotationNamedListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @ApiStatus.Internal
    @Override
    public AsmrAnnotationNamedListNode<V, E> newInstance(AsmrNode<?> parent) {
        return new AsmrAnnotationNamedListNode<>(parent);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected E newElement(Type<?> type) {
        if (type == EnumReferenceType.INSTANCE) {
            return (E) new AsmrNamedEnumReferenceNode(this);
        } else if (type == AnnotationType.INSTANCE) {
            return (E) new AsmrNamedAnnotationNode(this);
        } else if (type == ArrayType.INSTANCE) {
            return (E) new AsmrNamedAnnotationValueListNode<>(this);
        } else if (type instanceof AnnotationNamedListType) {
            return (E) new AsmrNamedValueNode<>(this);
        } else {
            throw new IllegalArgumentException("Cannot create annotation element of type " + type);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Type<? extends E> getType(E element) {
        if (element instanceof AsmrNamedEnumReferenceNode) {
            return (Type<? extends E>) EnumReferenceType.INSTANCE;
        } else if (element instanceof AsmrNamedAnnotationNode) {
            return (Type<? extends E>) AnnotationType.INSTANCE;
        } else if (element instanceof AsmrNamedAnnotationValueListNode) {
            return (Type<? extends E>) ArrayType.INSTANCE;
        } else if (element instanceof AsmrNamedValueNode) {
            Class<?> valueType = ((AsmrNamedValueNode<?>) element).value().value().getClass();
            Type<? extends E> type = (Type<? extends E>) VALUE_TYPE_TO_TYPE.get(valueType);
            if (type != null) {
                return type;
            }
        }

        throw new IllegalArgumentException(element + " is not of an annotation value list type");
    }

    public interface AnnotationNamedListType<T> extends Type<T> {}
    public enum ByteType implements AnnotationNamedListType<AsmrNamedValueNode<Byte>> { INSTANCE }
    public enum BooleanType implements AnnotationNamedListType<AsmrNamedValueNode<Boolean>> { INSTANCE }
    public enum CharType implements AnnotationNamedListType<AsmrNamedValueNode<Character>> { INSTANCE }
    public enum ShortType implements AnnotationNamedListType<AsmrNamedValueNode<Short>> { INSTANCE }
    public enum IntType implements AnnotationNamedListType<AsmrNamedValueNode<Integer>> { INSTANCE }
    public enum LongType implements AnnotationNamedListType<AsmrNamedValueNode<Long>> { INSTANCE }
    public enum FloatType implements AnnotationNamedListType<AsmrNamedValueNode<Float>> { INSTANCE }
    public enum DoubleType implements AnnotationNamedListType<AsmrNamedValueNode<Double>> { INSTANCE }
    public enum StringType implements AnnotationNamedListType<AsmrNamedValueNode<String>> { INSTANCE }
    public enum ClassType implements AnnotationNamedListType<AsmrNamedValueNode<org.objectweb.asm.Type>> { INSTANCE }
    public enum EnumReferenceType implements AnnotationNamedListType<AsmrNamedEnumReferenceNode> { INSTANCE }
    public enum AnnotationType implements AnnotationNamedListType<AsmrNamedAnnotationNode> { INSTANCE }
    public static class ArrayType<T extends AsmrNode<T>> implements AnnotationNamedListType<AsmrNamedAnnotationValueListNode<T>> {
        private static final ArrayType<?> INSTANCE = new ArrayType<>();
        @SuppressWarnings("unchecked")
        public static <T extends AsmrNode<T>> ArrayType<T> instance() {
            return (ArrayType<T>) INSTANCE;
        }
        private ArrayType() {}
    }
}
