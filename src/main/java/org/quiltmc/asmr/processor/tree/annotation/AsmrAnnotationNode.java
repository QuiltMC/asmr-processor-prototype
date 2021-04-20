package org.quiltmc.asmr.processor.tree.annotation;

import org.quiltmc.asmr.processor.tree.AsmrNode;

import java.util.Arrays;
import java.util.List;

public class AsmrAnnotationNode extends AsmrAbstractAnnotationNode<AsmrAnnotationNode> {
    private final List<AsmrNode<?>> children = Arrays.asList(desc(), values());

    public AsmrAnnotationNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    protected AsmrAnnotationNode newInstance(AsmrNode<?> parent) {
        return new AsmrAnnotationNode(parent);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }
}
