package org.quiltmc.asmr.processor.tree.annotation;

import org.quiltmc.asmr.processor.tree.AsmrNode;
import org.quiltmc.asmr.processor.tree.AsmrValueNode;

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
    protected AsmrEnumReferenceNode newInstance(AsmrNode<?> parent) {
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
