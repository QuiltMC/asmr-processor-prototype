package org.quiltmc.asmr.processor.tree;

import org.objectweb.asm.AnnotationVisitor;

public abstract class AsmrAbstractAnnotationNode<SELF extends AsmrAbstractAnnotationNode<SELF>> extends AsmrNode<SELF> {
    private final AsmrValueNode<String> descriptor = new AsmrValueNode<>(this);

    public AsmrAbstractAnnotationNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    void copyFrom(SELF other) {
        descriptor.copyFrom(((AsmrAbstractAnnotationNode<?>) other).descriptor);
    }

    public AsmrValueNode<String> descriptor() {
        return descriptor;
    }

    public void accept(AnnotationVisitor av) {
        av.visitEnd();
    }
}
