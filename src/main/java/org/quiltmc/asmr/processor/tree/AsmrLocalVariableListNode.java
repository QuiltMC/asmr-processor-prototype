package org.quiltmc.asmr.processor.tree;

public class AsmrLocalVariableListNode extends AsmrListNode<AsmrLocalVariableNode, AsmrLocalVariableListNode> {
    public AsmrLocalVariableListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrLocalVariableListNode newInstance(AsmrNode<?> parent) {
        return new AsmrLocalVariableListNode(parent);
    }

    @Override
    AsmrLocalVariableNode newElement() {
        return new AsmrLocalVariableNode(this);
    }
}
