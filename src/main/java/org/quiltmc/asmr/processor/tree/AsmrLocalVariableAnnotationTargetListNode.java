package org.quiltmc.asmr.processor.tree;

public class AsmrLocalVariableAnnotationTargetListNode extends AsmrListNode<AsmrLocalVariableAnnotationTargetNode, AsmrLocalVariableAnnotationTargetListNode> {
    public AsmrLocalVariableAnnotationTargetListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrLocalVariableAnnotationTargetListNode newInstance(AsmrNode<?> parent) {
        return new AsmrLocalVariableAnnotationTargetListNode(parent);
    }

    @Override
    AsmrLocalVariableAnnotationTargetNode newElement() {
        return new AsmrLocalVariableAnnotationTargetNode(this);
    }
}
