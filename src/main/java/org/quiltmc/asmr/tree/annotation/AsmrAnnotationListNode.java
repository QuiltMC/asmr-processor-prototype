package org.quiltmc.asmr.tree.annotation;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.tree.AsmrListNode;
import org.quiltmc.asmr.tree.AsmrNode;

public class AsmrAnnotationListNode extends AsmrListNode<AsmrAnnotationNode, AsmrAnnotationListNode> {
    public AsmrAnnotationListNode() {
        this(null);
    }

    @ApiStatus.Internal
    public AsmrAnnotationListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @ApiStatus.Internal
    @Override
    public AsmrAnnotationListNode newInstance(AsmrNode<?> parent) {
        return new AsmrAnnotationListNode(parent);
    }

    @Override
    protected AsmrAnnotationNode newElement() {
        return new AsmrAnnotationNode(this);
    }
}
