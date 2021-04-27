package org.quiltmc.asmr.processor.tree.method;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.processor.tree.AsmrNode;
import org.quiltmc.asmr.processor.tree.AsmrValueListNode;
import org.quiltmc.asmr.processor.tree.AsmrValueNode;

import java.util.Arrays;
import java.util.List;

public class AsmrParameterNode extends AsmrNode<AsmrParameterNode> {
    private final AsmrValueNode<String> name = new AsmrValueNode<>(this);
    private final AsmrValueListNode<Integer> modifiers = new AsmrValueListNode<>(this);

    private final List<AsmrNode<?>> children = Arrays.asList(name, modifiers);

    public AsmrParameterNode() {
        this(null);
    }

    @ApiStatus.Internal
    public AsmrParameterNode(AsmrNode<?> parent) {
        super(parent);
    }

    @ApiStatus.Internal
    @Override
    public AsmrParameterNode newInstance(AsmrNode<?> parent) {
        return new AsmrParameterNode(parent);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }

    public AsmrValueNode<String> name() {
        return name;
    }

    public AsmrValueListNode<Integer> modifiers() {
        return modifiers;
    }
}
