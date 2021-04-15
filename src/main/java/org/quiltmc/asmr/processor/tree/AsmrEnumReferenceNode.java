package org.quiltmc.asmr.processor.tree;

import java.util.Arrays;
import java.util.List;

public class AsmrEnumReferenceNode extends AsmrNode<AsmrEnumReferenceNode> {
    private final AsmrValueNode<String> owner = new AsmrValueNode<>(this);
    private final AsmrValueNode<String> name = new AsmrValueNode<>(this);

    private final List<AsmrNode<?>> children = Arrays.asList(owner, name);

    public AsmrEnumReferenceNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrEnumReferenceNode newInstance(AsmrNode<?> parent) {
        return new AsmrEnumReferenceNode(parent);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }

    @Override
    void copyFrom(AsmrEnumReferenceNode other) {
        owner.copyFrom(other.owner);
        name.copyFrom(other.name);
    }

    public AsmrValueNode<String> owner() {
        return owner;
    }

    public AsmrValueNode<String> name() {
        return name;
    }
}
