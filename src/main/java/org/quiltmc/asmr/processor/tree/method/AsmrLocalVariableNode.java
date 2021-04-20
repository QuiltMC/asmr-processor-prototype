package org.quiltmc.asmr.processor.tree.method;

import org.quiltmc.asmr.processor.tree.AsmrNode;
import org.quiltmc.asmr.processor.tree.AsmrValueNode;

import java.util.Arrays;
import java.util.List;

public class AsmrLocalVariableNode extends AsmrNode<AsmrLocalVariableNode> {
    private final AsmrValueNode<String> name = new AsmrValueNode<>(this);
    private final AsmrValueNode<String> desc = new AsmrValueNode<>(this);
    private final AsmrValueNode<String> signature = new AsmrValueNode<>(this);
    private final AsmrValueNode<AsmrIndex> start = new AsmrValueNode<>(this);
    private final AsmrValueNode<AsmrIndex> end = new AsmrValueNode<>(this);
    private final AsmrValueNode<AsmrIndex> index = new AsmrValueNode<>(this);

    private final List<AsmrNode<?>> children = Arrays.asList(name, desc, signature, start, end, index);

    public AsmrLocalVariableNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    protected AsmrLocalVariableNode newInstance(AsmrNode<?> parent) {
        return new AsmrLocalVariableNode(parent);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }

    public AsmrValueNode<String> name() {
        return name;
    }

    public AsmrValueNode<String> desc() {
        return desc;
    }

    public AsmrValueNode<String> signature() {
        return signature;
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
