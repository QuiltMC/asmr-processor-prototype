package org.quiltmc.asmr.processor.tree;

import java.util.ArrayList;
import java.util.List;

public class AsmrValueListNode<T> extends AsmrListNode<AsmrValueNode<T>, AsmrValueListNode<T>> {
    public AsmrValueListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrValueListNode<T> newInstance(AsmrNode<?> parent) {
        return new AsmrValueListNode<>(parent);
    }

    @Override
    AsmrValueNode<T> newElement() {
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
