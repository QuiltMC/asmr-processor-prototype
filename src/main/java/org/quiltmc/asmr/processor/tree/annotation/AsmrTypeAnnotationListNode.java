package org.quiltmc.asmr.processor.tree.annotation;

import org.quiltmc.asmr.processor.tree.AsmrListNode;
import org.quiltmc.asmr.processor.tree.AsmrNode;

public class AsmrTypeAnnotationListNode extends AsmrListNode<AsmrTypeAnnotationNode, AsmrTypeAnnotationListNode> {
    public AsmrTypeAnnotationListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    protected AsmrTypeAnnotationListNode newInstance(AsmrNode<?> parent) {
        return new AsmrTypeAnnotationListNode(parent);
    }

    @Override
    protected AsmrTypeAnnotationNode newElement() {
        return new AsmrTypeAnnotationNode(this);
    }
}
