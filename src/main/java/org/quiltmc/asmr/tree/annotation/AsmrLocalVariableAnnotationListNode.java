package org.quiltmc.asmr.tree.annotation;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.tree.AsmrListNode;
import org.quiltmc.asmr.tree.AsmrNode;

public class AsmrLocalVariableAnnotationListNode extends AsmrListNode<AsmrLocalVariableAnnotationNode, AsmrLocalVariableAnnotationListNode> {
    public AsmrLocalVariableAnnotationListNode() {
        this(null);
    }

    @ApiStatus.Internal
    public AsmrLocalVariableAnnotationListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @ApiStatus.Internal
    @Override
    public AsmrLocalVariableAnnotationListNode newInstance(AsmrNode<?> parent) {
        return new AsmrLocalVariableAnnotationListNode(parent);
    }

    @Override
    protected AsmrLocalVariableAnnotationNode newElement() {
        return new AsmrLocalVariableAnnotationNode(this);
    }
}
