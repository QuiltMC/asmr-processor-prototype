package org.quiltmc.asmr.tree.annotation;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.tree.AsmrNode;
import org.quiltmc.asmr.tree.AsmrValueNode;

import java.util.Arrays;
import java.util.List;

public abstract class AsmrNamedNode<V extends AsmrNode<V>, SELF extends AsmrNamedNode<V, SELF>> extends AsmrNode<SELF> {
    private final AsmrValueNode<String> name = new AsmrValueNode<>(this);
    private final V value = newValue();

    private final List<AsmrNode<?>> children = Arrays.asList(name, value);

    public AsmrNamedNode() {
        this(null);
    }

    @ApiStatus.Internal
    public AsmrNamedNode(AsmrNode<?> parent) {
        super(parent);
    }

    abstract V newValue();

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }

    public AsmrValueNode<String> name() {
        return name;
    }

    public V value() {
        return value;
    }
}
