package org.quiltmc.asmr.processor.tree.annotation;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.processor.tree.AsmrListNode;
import org.quiltmc.asmr.processor.tree.AsmrNode;

public class AsmrLocalVariableAnnotationListNode extends AsmrListNode<AsmrLocalVariableAnnotationNode, AsmrLocalVariableAnnotationListNode> {
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
