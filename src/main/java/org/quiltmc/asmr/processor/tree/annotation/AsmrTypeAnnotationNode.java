package org.quiltmc.asmr.processor.tree.annotation;

import org.quiltmc.asmr.processor.tree.AsmrNode;
import org.quiltmc.asmr.processor.tree.AsmrValueNode;

import java.util.Arrays;
import java.util.List;

public class AsmrTypeAnnotationNode extends AsmrAbstractAnnotationNode<AsmrTypeAnnotationNode> {
    private final AsmrValueNode<Integer> typeRef = new AsmrValueNode<>(this);
    private final AsmrValueNode<String> typePath = new AsmrValueNode<>(this);

    private final List<AsmrNode<?>> children = Arrays.asList(desc(), values(), typeRef, typePath);

    public AsmrTypeAnnotationNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    protected AsmrTypeAnnotationNode newInstance(AsmrNode<?> parent) {
        return new AsmrTypeAnnotationNode(parent);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }

    public AsmrValueNode<Integer> typeRef() {
        return typeRef;
    }

    public AsmrValueNode<String> typePath() {
        return typePath;
    }
}
