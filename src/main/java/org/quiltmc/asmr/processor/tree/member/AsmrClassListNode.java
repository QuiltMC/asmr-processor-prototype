package org.quiltmc.asmr.processor.tree.member;

import org.quiltmc.asmr.processor.tree.AsmrListNode;
import org.quiltmc.asmr.processor.tree.AsmrNode;

public class AsmrClassListNode extends AsmrListNode<AsmrClassNode, AsmrClassListNode> {
    public AsmrClassListNode() {
        super(null);
    }

    @Override
    protected AsmrClassListNode newInstance(AsmrNode<?> parent) {
        return new AsmrClassListNode();
    }

    @Override
    protected AsmrClassNode newElement() {
        return new AsmrClassNode(this);
    }
}
