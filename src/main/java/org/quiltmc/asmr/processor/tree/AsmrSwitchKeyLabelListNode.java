package org.quiltmc.asmr.processor.tree;

public class AsmrSwitchKeyLabelListNode extends AsmrListNode<AsmrSwitchKeyLabelNode, AsmrSwitchKeyLabelListNode> {
    public AsmrSwitchKeyLabelListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrSwitchKeyLabelListNode newInstance(AsmrNode<?> parent) {
        return new AsmrSwitchKeyLabelListNode(parent);
    }

    @Override
    AsmrSwitchKeyLabelNode newElement() {
        return new AsmrSwitchKeyLabelNode(this);
    }
}
