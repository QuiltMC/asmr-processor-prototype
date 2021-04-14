package org.quiltmc.asmr.processor.tree;

import java.util.Collections;
import java.util.List;

public class AsmrValueNode<T> extends AsmrNode<AsmrValueNode<T>> {
    private T value;

    AsmrValueNode(AsmrNode<?> parent) {
        super(parent);
    }

    public AsmrValueNode<T> init(T value) {
        if (this.value != null) {
            throw new IllegalStateException("Already initialized AsmrValueNode");
        }
        this.value = value;
        return this;
    }

    public T value() {
        if (value == null) {
            throw new IllegalStateException("Uninitialized AsmrValueNode");
        }
        return value;
    }

    @Override
    AsmrValueNode<T> newInstance(AsmrNode<?> parent) {
        return new AsmrValueNode<>(parent);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return Collections.emptyList();
    }

    @Override
    public void copyFrom(AsmrValueNode<T> other) {
        this.value = other.value();
    }
}
