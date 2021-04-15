package org.quiltmc.asmr.processor.tree;

import java.util.HashMap;
import java.util.Map;

public class AsmrAnnotationValueListNode<T extends AsmrNode<T>> extends AsmrPolymorphicListNode<T, AsmrAnnotationValueListNode<T>> {
    private static final Map<Class<?>, Class<?>> VALUE_TYPE_TO_TYPE = new HashMap<>();
    static {
        VALUE_TYPE_TO_TYPE.put(Byte.class, ByteType.class);
        VALUE_TYPE_TO_TYPE.put(Boolean.class, BooleanType.class);
        VALUE_TYPE_TO_TYPE.put(Character.class, CharType.class);
        VALUE_TYPE_TO_TYPE.put(Short.class, ShortType.class);
        VALUE_TYPE_TO_TYPE.put(Integer.class, IntType.class);
        VALUE_TYPE_TO_TYPE.put(Long.class, LongType.class);
        VALUE_TYPE_TO_TYPE.put(Float.class, FloatType.class);
        VALUE_TYPE_TO_TYPE.put(Double.class, DoubleType.class);
        VALUE_TYPE_TO_TYPE.put(String.class, StringType.class);
        VALUE_TYPE_TO_TYPE.put(org.objectweb.asm.Type.class, ClassType.class);
    }

    public AsmrAnnotationValueListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrAnnotationValueListNode<T> newInstance(AsmrNode<?> parent) {
        return new AsmrAnnotationValueListNode<>(parent);
    }

    @SuppressWarnings("unchecked")
    @Override
    T newElement(Class<? extends Type<? extends T>> type) {
        if ((Object) type == EnumReferenceType.class) {
            return (T) new AsmrEnumReferenceNode(this);
        } else if ((Object) type == AnnotationType.class) {
            return (T) new AsmrAnnotationNode(this);
        } else if ((Object) type == ArrayType.class) {
            return (T) new AsmrAnnotationValueListNode<>(this);
        } else if (AnnotationValueListType.class.isAssignableFrom(type)) {
            return (T) new AsmrValueNode<>(this);
        } else {
            throw new IllegalArgumentException("Cannot create annotation element of type " + type);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Type<? extends T>> getType(T element) {
        if (element instanceof AsmrEnumReferenceNode) {
            return (Class<? extends Type<T>>) (Object) EnumReferenceType.class;
        } else if (element instanceof AsmrAnnotationNode) {
            return (Class<? extends Type<T>>) (Object) AnnotationType.class;
        } else if (element instanceof AsmrAnnotationValueListNode) {
            return (Class<? extends Type<T>>) (Object) ArrayType.class;
        } else if (element instanceof AsmrValueNode) {
            Class<?> valueType = ((AsmrValueNode<?>) element).value().getClass();
            Class<? extends Type<T>> type = (Class<? extends Type<T>>) VALUE_TYPE_TO_TYPE.get(valueType);
            if (type != null) {
                return type;
            }
        }

        throw new IllegalArgumentException(element + " is not of an annotation value list type");
    }

    public interface AnnotationValueListType<T> extends Type<T> {}
    public interface ByteType extends AnnotationValueListType<AsmrValueNode<Byte>> {}
    public interface BooleanType extends AnnotationValueListType<AsmrValueNode<Boolean>> {}
    public interface CharType extends AnnotationValueListType<AsmrValueNode<Character>> {}
    public interface ShortType extends AnnotationValueListType<AsmrValueNode<Short>> {}
    public interface IntType extends AnnotationValueListType<AsmrValueNode<Integer>> {}
    public interface LongType extends AnnotationValueListType<AsmrValueNode<Long>> {}
    public interface FloatType extends AnnotationValueListType<AsmrValueNode<Float>> {}
    public interface DoubleType extends AnnotationValueListType<AsmrValueNode<Double>> {}
    public interface StringType extends AnnotationValueListType<AsmrValueNode<String>> {}
    public interface ClassType extends AnnotationValueListType<AsmrValueNode<org.objectweb.asm.Type>> {}
    public interface EnumReferenceType extends AnnotationValueListType<AsmrEnumReferenceNode> {}
    public interface AnnotationType extends AnnotationValueListType<AsmrAnnotationNode> {}
    public interface ArrayType extends AnnotationValueListType<AsmrAnnotationValueListNode<?>> {}
}
