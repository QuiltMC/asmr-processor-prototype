package org.quiltmc.asmr.processor.tree.annotation;

import org.quiltmc.asmr.processor.tree.AsmrNode;
import org.quiltmc.asmr.processor.tree.AsmrValueNode;
import org.quiltmc.asmr.processor.tree.method.AsmrIndex;

import java.util.Arrays;
import java.util.List;

public class AsmrLocalVariableAnnotationTargetNode extends AsmrNode<AsmrLocalVariableAnnotationTargetNode> {
    private final AsmrValueNode<AsmrIndex> start = new AsmrValueNode<>(this);
    private final AsmrValueNode<AsmrIndex> end = new AsmrValueNode<>(this);
    private final AsmrValueNode<AsmrIndex> index = new AsmrValueNode<>(this);

    private final List<AsmrNode<?>> children = Arrays.asList(start, end, index);

    public AsmrLocalVariableAnnotationTargetNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    protected AsmrLocalVariableAnnotationTargetNode newInstance(AsmrNode<?> parent) {
        return new AsmrLocalVariableAnnotationTargetNode(parent);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }

    public AsmrValueNode<AsmrIndex> start() {
        return start;
    }

    public AsmrValueNode<AsmrIndex> end() {
        return end;
    }

    public AsmrValueNode<AsmrIndex> index() {
        return index;
    }
}
