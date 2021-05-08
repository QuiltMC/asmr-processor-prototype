package org.quiltmc.asmr.tree.annotation;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.tree.AsmrNode;
import org.quiltmc.asmr.tree.AsmrValueNode;

import java.util.Arrays;
import java.util.List;

public class AsmrEnumReferenceNode extends AsmrNode<AsmrEnumReferenceNode> {
    private final AsmrValueNode<String> owner = new AsmrValueNode<>(this);
    private final AsmrValueNode<String> name = new AsmrValueNode<>(this);

    private final List<AsmrNode<?>> children = Arrays.asList(owner, name);

    public AsmrEnumReferenceNode() {
        this(null);
    }

    @ApiStatus.Internal
    public AsmrEnumReferenceNode(AsmrNode<?> parent) {
        super(parent);
    }

    @ApiStatus.Internal
    @Override
    public AsmrEnumReferenceNode newInstance(AsmrNode<?> parent) {
        return new AsmrEnumReferenceNode(parent);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }

    public AsmrValueNode<String> owner() {
        return owner;
    }

    public AsmrValueNode<String> name() {
        return name;
    }
}
