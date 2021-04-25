package org.quiltmc.asmr.processor.tree.method;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.processor.tree.AsmrListNode;
import org.quiltmc.asmr.processor.tree.AsmrNode;

public class AsmrTryCatchBlockListNode extends AsmrListNode<AsmrTryCatchBlockNode, AsmrTryCatchBlockListNode> {
    public AsmrTryCatchBlockListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @ApiStatus.Internal
    @Override
    public AsmrTryCatchBlockListNode newInstance(AsmrNode<?> parent) {
        return new AsmrTryCatchBlockListNode(parent);
    }

    @Override
    protected AsmrTryCatchBlockNode newElement() {
        return new AsmrTryCatchBlockNode(this);
    }
}
