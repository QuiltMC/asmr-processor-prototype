package org.quiltmc.asmr.processor.tree;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

public class AsmrValueListNode<T> extends AsmrListNode<AsmrValueNode<T>, AsmrValueListNode<T>> {
    public AsmrValueListNode() {
        this(null);
    }

    @ApiStatus.Internal
    public AsmrValueListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @ApiStatus.Internal
    @Override
    public AsmrValueListNode<T> newInstance(AsmrNode<?> parent) {
        return new AsmrValueListNode<>(parent);
    }

    @Override
    protected AsmrValueNode<T> newElement() {
        return new AsmrValueNode<>(this);
    }

    public T[] toArray(T[] dest) {
        List<AsmrValueNode<T>> nodes = typedChildren();
        List<T> list = new ArrayList<>(nodes.size());
        for (AsmrValueNode<T> node : nodes) {
            list.add(node.value());
        }
        return list.toArray(dest);
    }
}
