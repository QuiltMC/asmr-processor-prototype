package org.quiltmc.asmr.tree.annotation;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.tree.AsmrListNode;
import org.quiltmc.asmr.tree.AsmrNode;

public class AsmrTypeAnnotationListNode extends AsmrListNode<AsmrTypeAnnotationNode, AsmrTypeAnnotationListNode> {
    public AsmrTypeAnnotationListNode() {
        this(null);
    }

    @ApiStatus.Internal
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
