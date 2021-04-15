package org.quiltmc.asmr.processor.tree;

public class AsmrInnerClassListNode extends AsmrListNode<AsmrInnerClassNode, AsmrInnerClassListNode> {
    public AsmrInnerClassListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrInnerClassListNode newInstance(AsmrNode<?> parent) {
        return new AsmrInnerClassListNode(parent);
    }

    @Override
    AsmrInnerClassNode newElement() {
        return new AsmrInnerClassNode(this);
    }
}
