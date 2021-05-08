package org.quiltmc.asmr.tree.annotation;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.tree.AsmrNode;

import java.util.Arrays;
import java.util.List;

public class AsmrAnnotationNode extends AsmrAbstractAnnotationNode<AsmrAnnotationNode> {
    private final List<AsmrNode<?>> children = Arrays.asList(desc(), values());

    public AsmrAnnotationNode() {
        this(null);
    }

    @ApiStatus.Internal
    public AsmrAnnotationNode(AsmrNode<?> parent) {
        super(parent);
    }

    @ApiStatus.Internal
    @Override
    public AsmrAnnotationNode newInstance(AsmrNode<?> parent) {
        return new AsmrAnnotationNode(parent);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }
}
