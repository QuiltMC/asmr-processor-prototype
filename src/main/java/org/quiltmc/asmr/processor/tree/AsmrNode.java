package org.quiltmc.asmr.processor.tree;

import java.util.List;

public abstract class AsmrNode<SELF extends AsmrNode<SELF>> {
    private final AsmrNode<?> parent;

    AsmrNode(AsmrNode<?> parent) {
        this.parent = parent;
    }

    @SuppressWarnings("unchecked")
    final SELF getThis() {
        return (SELF) this;
    }

    abstract SELF newInstance(AsmrNode<?> parent);

    public final AsmrNode<?> parent() {
        return parent;
    }

    public abstract List<AsmrNode<?>> children();

    abstract void copyFrom(SELF other);

    public SELF copy(AsmrNode<?> newParent) {
        SELF copy = newInstance(newParent);
        copy.copyFrom(getThis());
        return copy;
    }
}
