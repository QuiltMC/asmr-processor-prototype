package org.quiltmc.asmr.processor.tree.annotation;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.processor.tree.AsmrNode;
import org.quiltmc.asmr.processor.tree.AsmrValueNode;

import java.util.Arrays;
import java.util.List;

public class AsmrLocalVariableAnnotationNode extends AsmrAbstractAnnotationNode<AsmrLocalVariableAnnotationNode> {
    private final AsmrValueNode<Integer> typeRef = new AsmrValueNode<>(this);
    private final AsmrValueNode<String> typePath = new AsmrValueNode<>(this);
    private final AsmrLocalVariableAnnotationTargetListNode targets = new AsmrLocalVariableAnnotationTargetListNode(this);

    private final List<AsmrNode<?>> children = Arrays.asList(desc(), values(), typeRef, typePath, targets);

    public AsmrLocalVariableAnnotationNode() {
        this(null);
    }

    @ApiStatus.Internal
    public AsmrLocalVariableAnnotationNode(AsmrNode<?> parent) {
        super(parent);
    }

    @ApiStatus.Internal
    @Override
    public AsmrLocalVariableAnnotationNode newInstance(AsmrNode<?> parent) {
        return new AsmrLocalVariableAnnotationNode(parent);
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

    public AsmrLocalVariableAnnotationTargetListNode targets() {
        return targets;
    }
}
