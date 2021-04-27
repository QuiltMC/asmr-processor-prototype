package org.quiltmc.asmr.processor.tree.annotation;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.processor.tree.AsmrNode;

public class AsmrNamedAnnotationValueListNode<T extends AsmrNode<T>> extends AsmrNamedNode<AsmrAnnotationValueListNode<T>, AsmrNamedAnnotationValueListNode<T>> {
    public AsmrNamedAnnotationValueListNode() {
        this(null);
    }

    @ApiStatus.Internal
    public AsmrNamedAnnotationValueListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @ApiStatus.Internal
    @Override
    public AsmrNamedAnnotationValueListNode<T> newInstance(AsmrNode<?> parent) {
        return new AsmrNamedAnnotationValueListNode<>(parent);
    }

    @Override
    AsmrAnnotationValueListNode<T> newValue() {
        return new AsmrAnnotationValueListNode<>(this);
    }
}
