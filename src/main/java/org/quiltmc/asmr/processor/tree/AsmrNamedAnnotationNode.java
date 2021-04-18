package org.quiltmc.asmr.processor.tree;

public class AsmrNamedAnnotationNode extends AsmrNamedNode<AsmrAnnotationNode, AsmrNamedAnnotationNode> {
    public AsmrNamedAnnotationNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrNamedAnnotationNode newInstance(AsmrNode<?> parent) {
        return new AsmrNamedAnnotationNode(parent);
    }

    @Override
    AsmrAnnotationNode newValue() {
        return new AsmrAnnotationNode(this);
    }
}
