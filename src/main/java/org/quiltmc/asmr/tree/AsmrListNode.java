package org.quiltmc.asmr.tree;

import org.jetbrains.annotations.ApiStatus;

public abstract class AsmrListNode<E extends AsmrNode<E>, SELF extends AsmrListNode<E, SELF>> extends AsmrAbstractListNode<E, SELF> implements Iterable<E> {
    public AsmrListNode() {
        this(null);
    }

    @ApiStatus.Internal
    public AsmrListNode(AsmrNode<?> parent) {
        super(parent);
    }

    protected abstract E newElement();

    public E insert(int index) {
        ensureWritable();
        E element = newElement();
        children.add(index, element);
        return element;
    }

    public E add() {
        return insert(children.size());
    }
}
