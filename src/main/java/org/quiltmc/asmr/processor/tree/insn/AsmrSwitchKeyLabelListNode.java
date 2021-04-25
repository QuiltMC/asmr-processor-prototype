package org.quiltmc.asmr.processor.tree.insn;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.processor.tree.AsmrListNode;
import org.quiltmc.asmr.processor.tree.AsmrNode;

public class AsmrSwitchKeyLabelListNode extends AsmrListNode<AsmrSwitchKeyLabelNode, AsmrSwitchKeyLabelListNode> {
    public AsmrSwitchKeyLabelListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @ApiStatus.Internal
    @Override
    public AsmrSwitchKeyLabelListNode newInstance(AsmrNode<?> parent) {
        return new AsmrSwitchKeyLabelListNode(parent);
    }

    @Override
    protected AsmrSwitchKeyLabelNode newElement() {
        return new AsmrSwitchKeyLabelNode(this);
    }
}
