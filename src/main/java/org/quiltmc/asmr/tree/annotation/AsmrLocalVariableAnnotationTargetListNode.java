package org.quiltmc.asmr.tree.annotation;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.tree.AsmrListNode;
import org.quiltmc.asmr.tree.AsmrNode;

public class AsmrLocalVariableAnnotationTargetListNode extends AsmrListNode<AsmrLocalVariableAnnotationTargetNode, AsmrLocalVariableAnnotationTargetListNode> {
    public AsmrLocalVariableAnnotationTargetListNode() {
        this(null);
    }

    @ApiStatus.Internal
    public AsmrLocalVariableAnnotationTargetListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @ApiStatus.Internal
    @Override
    public AsmrLocalVariableAnnotationTargetListNode newInstance(AsmrNode<?> parent) {
        return new AsmrLocalVariableAnnotationTargetListNode(parent);
    }

    @Override
    protected AsmrLocalVariableAnnotationTargetNode newElement() {
        return new AsmrLocalVariableAnnotationTargetNode(this);
    }
}
