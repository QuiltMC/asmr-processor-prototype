package org.quiltmc.asmr.processor.tree.method;

import org.quiltmc.asmr.processor.tree.AsmrListNode;
import org.quiltmc.asmr.processor.tree.AsmrNode;

public class AsmrParameterListNode extends AsmrListNode<AsmrParameterNode, AsmrParameterListNode> {
    public AsmrParameterListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    protected AsmrParameterListNode newInstance(AsmrNode<?> parent) {
        return new AsmrParameterListNode(parent);
    }

    @Override
    protected AsmrParameterNode newElement() {
        return new AsmrParameterNode(this);
    }
}
