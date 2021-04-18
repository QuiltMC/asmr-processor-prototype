package org.quiltmc.asmr.processor.tree;

public class AsmrNamedEnumReferenceNode extends AsmrNamedNode<AsmrEnumReferenceNode, AsmrNamedEnumReferenceNode> {
    public AsmrNamedEnumReferenceNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrNamedEnumReferenceNode newInstance(AsmrNode<?> parent) {
        return new AsmrNamedEnumReferenceNode(parent);
    }

    @Override
    AsmrEnumReferenceNode newValue() {
        return new AsmrEnumReferenceNode(this);
    }
}
