package org.quiltmc.asmr.processor.tree.member;

import org.quiltmc.asmr.processor.tree.AsmrListNode;
import org.quiltmc.asmr.processor.tree.AsmrNode;

public class AsmrMethodListNode extends AsmrListNode<AsmrMethodNode, AsmrMethodListNode> {
    public AsmrMethodListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    protected AsmrMethodListNode newInstance(AsmrNode<?> parent) {
        return new AsmrMethodListNode(parent);
    }

    @Override
    protected AsmrMethodNode newElement() {
        return new AsmrMethodNode(this);
    }
}
