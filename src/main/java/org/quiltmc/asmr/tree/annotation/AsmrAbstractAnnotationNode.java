package org.quiltmc.asmr.tree.annotation;

import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.AnnotationVisitor;
import org.quiltmc.asmr.tree.AsmrNode;
import org.quiltmc.asmr.tree.AsmrPolymorphicListNode;
import org.quiltmc.asmr.tree.AsmrValueNode;

public abstract class AsmrAbstractAnnotationNode<SELF extends AsmrAbstractAnnotationNode<SELF>> extends AsmrNode<SELF> {
    private final AsmrValueNode<String> desc = new AsmrValueNode<>(this);
    private final AsmrAnnotationNamedListNode<?, ?> values = new AsmrAnnotationNamedListNode<>(this);

    public AsmrAbstractAnnotationNode() {
        this(null);
    }

    @ApiStatus.Internal
    public AsmrAbstractAnnotationNode(AsmrNode<?> parent) {
        super(parent);
    }

    public AsmrValueNode<String> desc() {
        return desc;
    }

    public AsmrAnnotationNamedListNode<?, ?> values() {
        return values;
    }

    public void accept(AnnotationVisitor av) {
        for (int i = 0; i < values.size(); i++) {
            AsmrPolymorphicListNode.Type<?> type = values.getType(i);
            if (type == AsmrAnnotationNamedListNode.EnumReferenceType.INSTANCE) {
                AsmrNamedEnumReferenceNode val = values.get(i, AsmrAnnotationNamedListNode.EnumReferenceType.INSTANCE);
                av.visitEnum(val.name().value(), val.value().owner().value(), val.value().name().value());
            } else if (type == AsmrAnnotationNamedListNode.AnnotationType.INSTANCE) {
                AsmrNamedAnnotationNode val = values.get(i, AsmrAnnotationNamedListNode.AnnotationType.INSTANCE);
                AnnotationVisitor subVisitor = av.visitAnnotation(val.name().value(), val.value().desc().value());
                if (subVisitor != null) {
                    val.value().accept(subVisitor);
                }
            } else if (type == AsmrAnnotationNamedListNode.ArrayType.instance()) {
                acceptNamedArray(av, i);
            } else {
                AsmrNamedValueNode<?> val = (AsmrNamedValueNode<?>) values.get(i);
                av.visit(val.name().value(), val.value().value());
            }
        }
        av.visitEnd();
    }

    private <T extends AsmrNode<T>> void acceptNamedArray(AnnotationVisitor av, int index) {
        AsmrNamedAnnotationValueListNode<T> namedArray = values.get(index, AsmrAnnotationNamedListNode.ArrayType.instance());
        AnnotationVisitor arrayVisitor = av.visitArray(namedArray.name().value());
        if (arrayVisitor != null) {
            acceptArray(arrayVisitor, namedArray.value());
        }
    }

    private static <T extends AsmrNode<T>> void acceptArrayInArray(AnnotationVisitor av, AsmrAnnotationValueListNode<?> parentArray, int index) {
        acceptArray(av, parentArray.get(index, AsmrAnnotationValueListNode.ArrayType.<T>instance()));
    }

    public static <T extends AsmrNode<T>> void acceptArray(AnnotationVisitor arrayVisitor, AsmrAnnotationValueListNode<T> array) {
        for (int i = 0; i < array.size(); i++) {
            AsmrPolymorphicListNode.Type<? extends T> type = array.getType(i);
            if (type == AsmrAnnotationValueListNode.EnumReferenceType.INSTANCE) {
                AsmrEnumReferenceNode val = array.get(i, AsmrAnnotationValueListNode.EnumReferenceType.INSTANCE);
                arrayVisitor.visitEnum(null, val.owner().value(), val.name().value());
            } else if (type == AsmrAnnotationValueListNode.AnnotationType.INSTANCE) {
                AsmrAnnotationNode val = array.get(i, AsmrAnnotationValueListNode.AnnotationType.INSTANCE);
                AnnotationVisitor subVisitor = arrayVisitor.visitAnnotation(null, val.desc().value());
                if (subVisitor != null) {
                    val.accept(subVisitor);
                }
            } else if (type == AsmrAnnotationValueListNode.ArrayType.instance()) {
                AnnotationVisitor subVisitor = arrayVisitor.visitArray(null);
                if (subVisitor != null) {
                    acceptArrayInArray(subVisitor, array, i);
                }
            } else {
                AsmrValueNode<?> val = (AsmrValueNode<?>) array.get(i);
                arrayVisitor.visit(null, val.value());
            }
        }
        arrayVisitor.visitEnd();
    }
}
