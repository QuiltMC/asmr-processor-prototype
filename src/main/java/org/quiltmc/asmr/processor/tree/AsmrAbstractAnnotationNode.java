package org.quiltmc.asmr.processor.tree;

import org.objectweb.asm.AnnotationVisitor;

public abstract class AsmrAbstractAnnotationNode<SELF extends AsmrAbstractAnnotationNode<SELF>> extends AsmrNode<SELF> {
    private final AsmrValueNode<String> desc = new AsmrValueNode<>(this);
    private final AsmrAnnotationNamedListNode<?, ?> values = new AsmrAnnotationNamedListNode<>(this);

    public AsmrAbstractAnnotationNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    void copyFrom(SELF other) {
        desc.copyFrom(((AsmrAbstractAnnotationNode<?>) other).desc);
        copyValues(values, ((AsmrAbstractAnnotationNode<?>) other).values);
    }

    @SuppressWarnings("unchecked")
    private static <V extends AsmrNode<V>, T extends AsmrNamedNode<V, T>> void copyValues(AsmrAnnotationNamedListNode<V, T> into, AsmrAnnotationNamedListNode<?, ?> from) {
        into.copyFrom((AsmrAnnotationNamedListNode<V, T>) from);
    }

    public AsmrValueNode<String> desc() {
        return desc;
    }

    public AsmrAnnotationNamedListNode<?, ?> values() {
        return values;
    }

    public void accept(AnnotationVisitor av) {
        av.visitEnd();
    }
}
