package org.quiltmc.asmr.processor.tree.annotation;

import org.quiltmc.asmr.processor.tree.AsmrListNode;
import org.quiltmc.asmr.processor.tree.AsmrNode;

public class AsmrLocalVariableAnnotationTargetListNode extends AsmrListNode<AsmrLocalVariableAnnotationTargetNode, AsmrLocalVariableAnnotationTargetListNode> {
    public AsmrLocalVariableAnnotationTargetListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    protected AsmrLocalVariableAnnotationTargetListNode newInstance(AsmrNode<?> parent) {
        return new AsmrLocalVariableAnnotationTargetListNode(parent);
    }

    @Override
    protected AsmrLocalVariableAnnotationTargetNode newElement() {
        return new AsmrLocalVariableAnnotationTargetNode(this);
    }
}
