package org.quiltmc.asmr.processor.tree.annotation;

import org.quiltmc.asmr.processor.tree.AsmrNode;

public class AsmrNamedAnnotationNode extends AsmrNamedNode<AsmrAnnotationNode, AsmrNamedAnnotationNode> {
    public AsmrNamedAnnotationNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    protected AsmrNamedAnnotationNode newInstance(AsmrNode<?> parent) {
        return new AsmrNamedAnnotationNode(parent);
    }

    @Override
    AsmrAnnotationNode newValue() {
        return new AsmrAnnotationNode(this);
    }
}
