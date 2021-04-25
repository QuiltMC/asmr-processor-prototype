package org.quiltmc.asmr.processor.tree.annotation;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.processor.tree.AsmrListNode;
import org.quiltmc.asmr.processor.tree.AsmrNode;

public class AsmrTypeAnnotationListNode extends AsmrListNode<AsmrTypeAnnotationNode, AsmrTypeAnnotationListNode> {
    public AsmrTypeAnnotationListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @ApiStatus.Internal
    @Override
    public AsmrTypeAnnotationListNode newInstance(AsmrNode<?> parent) {
        return new AsmrTypeAnnotationListNode(parent);
    }

    @Override
    protected AsmrTypeAnnotationNode newElement() {
        return new AsmrTypeAnnotationNode(this);
    }
}
