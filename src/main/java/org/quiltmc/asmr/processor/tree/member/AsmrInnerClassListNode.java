package org.quiltmc.asmr.processor.tree.member;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.processor.tree.AsmrListNode;
import org.quiltmc.asmr.processor.tree.AsmrNode;

public class AsmrInnerClassListNode extends AsmrListNode<AsmrInnerClassNode, AsmrInnerClassListNode> {
    public AsmrInnerClassListNode() {
        this(null);
    }

    @ApiStatus.Internal
    public AsmrInnerClassListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @ApiStatus.Internal
    @Override
    public AsmrInnerClassListNode newInstance(AsmrNode<?> parent) {
        return new AsmrInnerClassListNode(parent);
    }

    @Override
    protected AsmrInnerClassNode newElement() {
        return new AsmrInnerClassNode(this);
    }
}
