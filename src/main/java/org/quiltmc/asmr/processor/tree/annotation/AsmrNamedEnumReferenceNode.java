package org.quiltmc.asmr.processor.tree.annotation;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.processor.tree.AsmrNode;

public class AsmrNamedEnumReferenceNode extends AsmrNamedNode<AsmrEnumReferenceNode, AsmrNamedEnumReferenceNode> {
    public AsmrNamedEnumReferenceNode(AsmrNode<?> parent) {
        super(parent);
    }

    @ApiStatus.Internal
    @Override
    public AsmrNamedEnumReferenceNode newInstance(AsmrNode<?> parent) {
        return new AsmrNamedEnumReferenceNode(parent);
    }

    @Override
    protected AsmrEnumReferenceNode newValue() {
        return new AsmrEnumReferenceNode(this);
    }
}
