package org.quiltmc.asmr.processor.tree;

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
    AsmrTypeAnnotationNode newInstance(AsmrNode<?> parent) {
        return new AsmrTypeAnnotationNode(parent);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }

    @Override
    void copyFrom(AsmrTypeAnnotationNode other) {
        super.copyFrom(other);
        typeRef.copyFrom(other.typeRef);
        typePath.copyFrom(other.typePath);
    }

    public AsmrValueNode<Integer> typeRef() {
        return typeRef;
    }

    public AsmrValueNode<String> typePath() {
        return typePath;
    }
}
