package org.quiltmc.asmr.processor.tree.method;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.processor.tree.AsmrListNode;
import org.quiltmc.asmr.processor.tree.AsmrNode;

public class AsmrParameterListNode extends AsmrListNode<AsmrParameterNode, AsmrParameterListNode> {
    public AsmrParameterListNode() {
        this(null);
    }

    @ApiStatus.Internal
    public AsmrParameterListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @ApiStatus.Internal
    @Override
    public AsmrParameterListNode newInstance(AsmrNode<?> parent) {
        return new AsmrParameterListNode(parent);
    }

    @Override
    protected AsmrParameterNode newElement() {
        return new AsmrParameterNode(this);
    }
}
