package org.quiltmc.asmr.processor.tree;

public class AsmrNamedAnnotationValueListNode<T extends AsmrNode<T>> extends AsmrNamedNode<AsmrAnnotationValueListNode<T>, AsmrNamedAnnotationValueListNode<T>> {
    public AsmrNamedAnnotationValueListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrNamedAnnotationValueListNode<T> newInstance(AsmrNode<?> parent) {
        return new AsmrNamedAnnotationValueListNode<>(parent);
    }

    @Override
    AsmrAnnotationValueListNode<T> newValue() {
        return new AsmrAnnotationValueListNode<>(this);
    }
}
