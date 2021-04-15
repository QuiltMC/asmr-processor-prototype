package org.quiltmc.asmr.processor.tree;

public class AsmrFieldListNode extends AsmrListNode<AsmrFieldNode, AsmrFieldListNode> {
    public AsmrFieldListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrFieldListNode newInstance(AsmrNode<?> parent) {
        return new AsmrFieldListNode(parent);
    }

    @Override
    AsmrFieldNode newElement() {
        return new AsmrFieldNode(this);
    }
}
