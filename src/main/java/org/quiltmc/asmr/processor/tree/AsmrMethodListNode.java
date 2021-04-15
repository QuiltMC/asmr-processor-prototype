package org.quiltmc.asmr.processor.tree;

public class AsmrMethodListNode extends AsmrListNode<AsmrMethodNode, AsmrMethodListNode> {
    public AsmrMethodListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrMethodListNode newInstance(AsmrNode<?> parent) {
        return new AsmrMethodListNode(parent);
    }

    @Override
    AsmrMethodNode newElement() {
        return new AsmrMethodNode(this);
    }
}
