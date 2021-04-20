package org.quiltmc.asmr.processor.tree.method;

import org.quiltmc.asmr.processor.tree.AsmrListNode;
import org.quiltmc.asmr.processor.tree.AsmrNode;

public class AsmrLocalVariableListNode extends AsmrListNode<AsmrLocalVariableNode, AsmrLocalVariableListNode> {
    public AsmrLocalVariableListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    protected AsmrLocalVariableListNode newInstance(AsmrNode<?> parent) {
        return new AsmrLocalVariableListNode(parent);
    }

    @Override
    protected AsmrLocalVariableNode newElement() {
        return new AsmrLocalVariableNode(this);
    }
}
