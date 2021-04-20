package org.quiltmc.asmr.processor.tree.annotation;

import org.quiltmc.asmr.processor.tree.AsmrNode;

public class AsmrNamedAnnotationValueListNode<T extends AsmrNode<T>> extends AsmrNamedNode<AsmrAnnotationValueListNode<T>, AsmrNamedAnnotationValueListNode<T>> {
    public AsmrNamedAnnotationValueListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    protected AsmrNamedAnnotationValueListNode<T> newInstance(AsmrNode<?> parent) {
        return new AsmrNamedAnnotationValueListNode<>(parent);
    }

    @Override
    AsmrAnnotationValueListNode<T> newValue() {
        return new AsmrAnnotationValueListNode<>(this);
    }
}
