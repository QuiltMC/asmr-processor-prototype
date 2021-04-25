package org.quiltmc.asmr.processor.tree.member;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.processor.tree.AsmrListNode;
import org.quiltmc.asmr.processor.tree.AsmrNode;

public class AsmrFieldListNode extends AsmrListNode<AsmrFieldNode, AsmrFieldListNode> {
    public AsmrFieldListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @ApiStatus.Internal
    @Override
    public AsmrFieldListNode newInstance(AsmrNode<?> parent) {
        return new AsmrFieldListNode(parent);
    }

    @Override
    protected AsmrFieldNode newElement() {
        return new AsmrFieldNode(this);
    }
}
