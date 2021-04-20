package org.quiltmc.asmr.processor.tree.annotation;

import org.quiltmc.asmr.processor.tree.AsmrNode;
import org.quiltmc.asmr.processor.tree.AsmrValueNode;

public class AsmrNamedValueNode<T> extends AsmrNamedNode<AsmrValueNode<T>, AsmrNamedValueNode<T>> {
    public AsmrNamedValueNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    protected AsmrValueNode<T> newValue() {
        return new AsmrValueNode<>(this);
    }

    @Override
    protected AsmrNamedValueNode<T> newInstance(AsmrNode<?> parent) {
        return new AsmrNamedValueNode<>(parent);
    }
}
