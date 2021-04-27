package org.quiltmc.asmr.processor.tree.annotation;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.processor.tree.AsmrNode;
import org.quiltmc.asmr.processor.tree.AsmrValueNode;

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
