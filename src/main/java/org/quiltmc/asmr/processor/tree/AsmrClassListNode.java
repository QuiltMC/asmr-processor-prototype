package org.quiltmc.asmr.processor.tree;

public class AsmrClassListNode extends AsmrListNode<AsmrClassNode, AsmrClassListNode> {
    public AsmrClassListNode() {
        super(null);
    }

    @Override
    AsmrClassListNode newInstance(AsmrNode<?> parent) {
        return new AsmrClassListNode();
    }

    @Override
    AsmrClassNode newElement() {
        return new AsmrClassNode(this);
    }
}
