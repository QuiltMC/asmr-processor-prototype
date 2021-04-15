package org.quiltmc.asmr.processor.tree;

public class AsmrAnnotationListNode extends AsmrListNode<AsmrAnnotationNode, AsmrAnnotationListNode> {
    public AsmrAnnotationListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrAnnotationListNode newInstance(AsmrNode<?> parent) {
        return new AsmrAnnotationListNode(parent);
    }

    @Override
    AsmrAnnotationNode newElement() {
        return new AsmrAnnotationNode(this);
    }
}
