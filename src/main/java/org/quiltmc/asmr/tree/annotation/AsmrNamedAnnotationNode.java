package org.quiltmc.asmr.tree.annotation;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.tree.AsmrNode;

public class AsmrNamedAnnotationNode extends AsmrNamedNode<AsmrAnnotationNode, AsmrNamedAnnotationNode> {
    public AsmrNamedAnnotationNode() {
        this(null);
    }

    @ApiStatus.Internal
    public AsmrNamedAnnotationNode(AsmrNode<?> parent) {
        super(parent);
    }

    @ApiStatus.Internal
    @Override
    public AsmrNamedAnnotationNode newInstance(AsmrNode<?> parent) {
        return new AsmrNamedAnnotationNode(parent);
    }

    @Override
    AsmrAnnotationNode newValue() {
        return new AsmrAnnotationNode(this);
    }
}
