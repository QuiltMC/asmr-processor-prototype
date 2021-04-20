package org.quiltmc.asmr.processor.tree;

public class AsmrLocalVariableAnnotationListNode extends AsmrListNode<AsmrLocalVariableAnnotationNode, AsmrLocalVariableAnnotationListNode> {
    public AsmrLocalVariableAnnotationListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrLocalVariableAnnotationListNode newInstance(AsmrNode<?> parent) {
        return new AsmrLocalVariableAnnotationListNode(parent);
    }

    @Override
    AsmrLocalVariableAnnotationNode newElement() {
        return new AsmrLocalVariableAnnotationNode(this);
    }
}
