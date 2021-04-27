package org.quiltmc.asmr.processor.tree.member;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.processor.tree.AsmrListNode;
import org.quiltmc.asmr.processor.tree.AsmrNode;

public class AsmrMethodListNode extends AsmrListNode<AsmrMethodNode, AsmrMethodListNode> {
    public AsmrMethodListNode() {
        this(null);
    }

    @ApiStatus.Internal
    public AsmrMethodListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @ApiStatus.Internal
    @Override
    public AsmrMethodListNode newInstance(AsmrNode<?> parent) {
        return new AsmrMethodListNode(parent);
    }

    @Override
    protected AsmrMethodNode newElement() {
        return new AsmrMethodNode(this);
    }
}
