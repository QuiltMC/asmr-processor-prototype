package org.quiltmc.asmr.tree.asmvisitor;

import org.objectweb.asm.AnnotationVisitor;
import org.quiltmc.asmr.processor.AsmrProcessor;
import org.quiltmc.asmr.tree.AsmrNode;
import org.quiltmc.asmr.tree.annotation.AsmrAbstractAnnotationNode;
import org.quiltmc.asmr.tree.annotation.AsmrAnnotationNamedListNode;
import org.quiltmc.asmr.tree.annotation.AsmrAnnotationNode;
import org.quiltmc.asmr.tree.annotation.AsmrAnnotationValueListNode;
import org.quiltmc.asmr.tree.annotation.AsmrEnumReferenceNode;
import org.quiltmc.asmr.tree.annotation.AsmrNamedAnnotationNode;
import org.quiltmc.asmr.tree.annotation.AsmrNamedAnnotationValueListNode;
import org.quiltmc.asmr.tree.annotation.AsmrNamedEnumReferenceNode;
import org.quiltmc.asmr.tree.annotation.AsmrNamedValueNode;

import java.lang.reflect.Array;

public class AsmrAnnotationVisitor extends AnnotationVisitor {
    private final AsmrAbstractAnnotationNode<?> annotationNode;

    public AsmrAnnotationVisitor(AsmrAbstractAnnotationNode<?> annotationNode) {
        super(AsmrProcessor.ASM_VERSION);
        this.annotationNode = annotationNode;
    }

    @Override
    public void visit(String name, Object value) {
        if (value instanceof Byte) {
            AsmrNamedValueNode<Byte> val = annotationNode.values().add(AsmrAnnotationNamedListNode.ByteType.INSTANCE);
            val.name().init(name);
            val.value().init((Byte) value);
        } else if (value instanceof Boolean) {
            AsmrNamedValueNode<Boolean> val = annotationNode.values().add(AsmrAnnotationNamedListNode.BooleanType.INSTANCE);
            val.name().init(name);
            val.value().init((Boolean) value);
        } else if (value instanceof Character) {
            AsmrNamedValueNode<Character> val = annotationNode.values().add(AsmrAnnotationNamedListNode.CharType.INSTANCE);
            val.name().init(name);
            val.value().init((Character) value);
        } else if (value instanceof Short) {
            AsmrNamedValueNode<Short> val = annotationNode.values().add(AsmrAnnotationNamedListNode.ShortType.INSTANCE);
            val.name().init(name);
            val.value().init((Short) value);
        } else if (value instanceof Integer) {
            AsmrNamedValueNode<Integer> val = annotationNode.values().add(AsmrAnnotationNamedListNode.IntType.INSTANCE);
            val.name().init(name);
            val.value().init((Integer) value);
        } else if (value instanceof Long) {
            AsmrNamedValueNode<Long> val = annotationNode.values().add(AsmrAnnotationNamedListNode.LongType.INSTANCE);
            val.name().init(name);
            val.value().init((Long) value);
        } else if (value instanceof Float) {
            AsmrNamedValueNode<Float> val = annotationNode.values().add(AsmrAnnotationNamedListNode.FloatType.INSTANCE);
            val.name().init(name);
            val.value().init((Float) value);
        } else if (value instanceof Double) {
            AsmrNamedValueNode<Double> val = annotationNode.values().add(AsmrAnnotationNamedListNode.DoubleType.INSTANCE);
            val.name().init(name);
            val.value().init((Double) value);
        } else if (value instanceof String) {
            AsmrNamedValueNode<String> val = annotationNode.values().add(AsmrAnnotationNamedListNode.StringType.INSTANCE);
            val.name().init(name);
            val.value().init((String) value);
        } else if (value instanceof org.objectweb.asm.Type) {
            AsmrNamedValueNode<org.objectweb.asm.Type> val = annotationNode.values().add(AsmrAnnotationNamedListNode.ClassType.INSTANCE);
            val.name().init(name);
            val.value().init((org.objectweb.asm.Type) value);
        } else if (value.getClass().isArray()) {
            AnnotationVisitor arrayVisitor = visitArray(name);
            for (int i = 0, e = Array.getLength(value); i < e; i++) {
                arrayVisitor.visit(null, Array.get(value, i));
            }
            arrayVisitor.visitEnd();
        } else {
            throw new IllegalArgumentException("Unknown value type: " + value.getClass().getName());
        }
    }

    @Override
    public void visitEnum(String name, String descriptor, String value) {
        AsmrNamedEnumReferenceNode val = annotationNode.values().add(AsmrAnnotationNamedListNode.EnumReferenceType.INSTANCE);
        val.name().init(name);
        val.value().owner().init(descriptor);
        val.value().name().init(value);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor) {
        AsmrNamedAnnotationNode val = annotationNode.values().add(AsmrAnnotationNamedListNode.AnnotationType.INSTANCE);
        val.name().init(name);
        val.value().desc().init(descriptor);
        return new AsmrAnnotationVisitor(val.value());
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        return doVisitArray(name);
    }

    private <T extends AsmrNode<T>> AnnotationVisitor doVisitArray(String name) {
        AsmrNamedAnnotationValueListNode<T> val = annotationNode.values().add(AsmrAnnotationNamedListNode.ArrayType.instance());
        val.name().init(name);
        return new ArrayVisitor<>(val.value());
    }

    public static class ArrayVisitor<T extends AsmrNode<T>> extends AnnotationVisitor {
        private final AsmrAnnotationValueListNode<T> arrayNode;

        public ArrayVisitor(AsmrAnnotationValueListNode<T> arrayNode) {
            super(AsmrProcessor.ASM_VERSION);
            this.arrayNode = arrayNode;
        }

        @Override
        public void visit(String name, Object value) {
            if (value instanceof Byte) {
                arrayNode.add(AsmrAnnotationValueListNode.ByteType.INSTANCE).init((Byte) value);
            } else if (value instanceof Boolean) {
                arrayNode.add(AsmrAnnotationValueListNode.BooleanType.INSTANCE).init((Boolean) value);
            } else if (value instanceof Character) {
                arrayNode.add(AsmrAnnotationValueListNode.CharType.INSTANCE).init((Character) value);
            } else if (value instanceof Short) {
                arrayNode.add(AsmrAnnotationValueListNode.ShortType.INSTANCE).init((Short) value);
            } else if (value instanceof Integer) {
                arrayNode.add(AsmrAnnotationValueListNode.IntType.INSTANCE).init((Integer) value);
            } else if (value instanceof Long) {
                arrayNode.add(AsmrAnnotationValueListNode.LongType.INSTANCE).init((Long) value);
            } else if (value instanceof Float) {
                arrayNode.add(AsmrAnnotationValueListNode.FloatType.INSTANCE).init((Float) value);
            } else if (value instanceof Double) {
                arrayNode.add(AsmrAnnotationValueListNode.DoubleType.INSTANCE).init((Double) value);
            } else if (value instanceof String) {
                arrayNode.add(AsmrAnnotationValueListNode.StringType.INSTANCE).init((String) value);
            } else if (value instanceof org.objectweb.asm.Type) {
                arrayNode.add(AsmrAnnotationValueListNode.ClassType.INSTANCE).init((org.objectweb.asm.Type) value);
            } else if (value.getClass().isArray()) {
                AnnotationVisitor arrayVisitor = visitArray(name);
                for (int i = 0, e = Array.getLength(value); i < e; i++) {
                    arrayVisitor.visit(null, Array.get(value, i));
                }
                arrayVisitor.visitEnd();
            } else {
                throw new IllegalArgumentException("Unknown value type: " + value.getClass().getName());
            }
        }

        @Override
        public void visitEnum(String name, String descriptor, String value) {
            AsmrEnumReferenceNode val = arrayNode.add(AsmrAnnotationValueListNode.EnumReferenceType.INSTANCE);
            val.owner().init(descriptor);
            val.name().init(value);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String descriptor) {
            AsmrAnnotationNode val = arrayNode.add(AsmrAnnotationValueListNode.AnnotationType.INSTANCE);
            val.desc().init(descriptor);
            return new AsmrAnnotationVisitor(val);
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            AsmrAnnotationValueListNode<T> val = arrayNode.add(AsmrAnnotationValueListNode.ArrayType.instance());
            return new ArrayVisitor<>(val);
        }
    }
}
