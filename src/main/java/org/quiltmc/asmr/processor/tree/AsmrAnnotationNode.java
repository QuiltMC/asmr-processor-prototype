package org.quiltmc.asmr.processor.tree;

import java.util.Arrays;
import java.util.List;

public class AsmrAnnotationNode extends AsmrAbstractAnnotationNode<AsmrAnnotationNode> {
    private final List<AsmrNode<?>> children = Arrays.asList(desc(), values());

    public AsmrAnnotationNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrAnnotationNode newInstance(AsmrNode<?> parent) {
        return new AsmrAnnotationNode(parent);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }
}
