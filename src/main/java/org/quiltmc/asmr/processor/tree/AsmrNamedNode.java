package org.quiltmc.asmr.processor.tree;

import java.util.Arrays;
import java.util.List;

public abstract class AsmrNamedNode<V extends AsmrNode<V>, SELF extends AsmrNamedNode<V, SELF>> extends AsmrNode<SELF> {
    private final AsmrValueNode<String> name = new AsmrValueNode<>(this);
    private final V value = newValue();

    private final List<AsmrNode<?>> children = Arrays.asList(name, value);

    public AsmrNamedNode(AsmrNode<?> parent) {
        super(parent);
    }

    abstract V newValue();

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }

    @Override
    void copyFrom(SELF other) {
        name.copyFrom(((AsmrNamedNode<V, SELF>) other).name);
        value.copyFrom(((AsmrNamedNode<V, SELF>) other).value);
    }

    public AsmrValueNode<String> name() {
        return name;
    }

    public V value() {
        return value;
    }
}
