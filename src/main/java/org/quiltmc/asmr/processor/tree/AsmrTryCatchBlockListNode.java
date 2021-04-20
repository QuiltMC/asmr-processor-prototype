package org.quiltmc.asmr.processor.tree;

public class AsmrTryCatchBlockListNode extends AsmrListNode<AsmrTryCatchBlockNode, AsmrTryCatchBlockListNode> {
    public AsmrTryCatchBlockListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrTryCatchBlockListNode newInstance(AsmrNode<?> parent) {
        return new AsmrTryCatchBlockListNode(parent);
    }

    @Override
    AsmrTryCatchBlockNode newElement() {
        return new AsmrTryCatchBlockNode(this);
    }
}
