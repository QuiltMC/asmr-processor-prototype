package org.quiltmc.asmr.processor.tree;

public class AsmrNamedValueNode<T> extends AsmrNamedNode<AsmrValueNode<T>, AsmrNamedValueNode<T>> {
    public AsmrNamedValueNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrValueNode<T> newValue() {
        return new AsmrValueNode<>(this);
    }

    @Override
    AsmrNamedValueNode<T> newInstance(AsmrNode<?> parent) {
        return new AsmrNamedValueNode<>(parent);
    }
}
