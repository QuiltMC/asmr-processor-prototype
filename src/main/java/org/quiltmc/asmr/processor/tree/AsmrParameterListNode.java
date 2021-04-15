package org.quiltmc.asmr.processor.tree;

public class AsmrParameterListNode extends AsmrListNode<AsmrParameterNode, AsmrParameterListNode> {
    public AsmrParameterListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrParameterListNode newInstance(AsmrNode<?> parent) {
        return new AsmrParameterListNode(parent);
    }

    @Override
    AsmrParameterNode newElement() {
        return new AsmrParameterNode(this);
    }
}
