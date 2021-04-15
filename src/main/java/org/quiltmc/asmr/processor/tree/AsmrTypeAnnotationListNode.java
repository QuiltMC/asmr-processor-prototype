package org.quiltmc.asmr.processor.tree;

public class AsmrTypeAnnotationListNode extends AsmrListNode<AsmrTypeAnnotationNode, AsmrTypeAnnotationListNode> {
    public AsmrTypeAnnotationListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrTypeAnnotationListNode newInstance(AsmrNode<?> parent) {
        return new AsmrTypeAnnotationListNode(parent);
    }

    @Override
    AsmrTypeAnnotationNode newElement() {
        return new AsmrTypeAnnotationNode(this);
    }
}
