package org.quiltmc.asmr.processor.tree;

public class AsmrAnnotationListListNode extends AsmrListNode<AsmrAnnotationListNode, AsmrAnnotationListListNode> {
    public AsmrAnnotationListListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrAnnotationListListNode newInstance(AsmrNode<?> parent) {
        return new AsmrAnnotationListListNode(parent);
    }

    @Override
    AsmrAnnotationListNode newElement() {
        return new AsmrAnnotationListNode(this);
    }
}
