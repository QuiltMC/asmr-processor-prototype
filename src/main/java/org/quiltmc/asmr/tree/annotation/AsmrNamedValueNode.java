package org.quiltmc.asmr.tree.annotation;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.tree.AsmrNode;
import org.quiltmc.asmr.tree.AsmrValueNode;

public class AsmrNamedValueNode<T> extends AsmrNamedNode<AsmrValueNode<T>, AsmrNamedValueNode<T>> {
    public AsmrNamedValueNode() {
        this(null);
    }

    @ApiStatus.Internal
    public AsmrNamedValueNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    protected AsmrValueNode<T> newValue() {
        return new AsmrValueNode<>(this);
    }

    @ApiStatus.Internal
    @Override
    public AsmrNamedValueNode<T> newInstance(AsmrNode<?> parent) {
        return new AsmrNamedValueNode<>(parent);
    }
}
