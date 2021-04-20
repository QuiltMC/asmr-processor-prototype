package org.quiltmc.asmr.processor.tree.annotation;

import org.quiltmc.asmr.processor.tree.AsmrListNode;
import org.quiltmc.asmr.processor.tree.AsmrNode;

public class AsmrAnnotationListListNode extends AsmrListNode<AsmrAnnotationListNode, AsmrAnnotationListListNode> {
    public AsmrAnnotationListListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    protected AsmrAnnotationListListNode newInstance(AsmrNode<?> parent) {
        return new AsmrAnnotationListListNode(parent);
    }

    @Override
    protected AsmrAnnotationListNode newElement() {
        return new AsmrAnnotationListNode(this);
    }
}
