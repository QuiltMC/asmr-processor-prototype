package org.quiltmc.asmr.processor.tree;

public abstract class AsmrListNode<E extends AsmrNode<E>, SELF extends AsmrListNode<E, SELF>> extends AsmrAbstractListNode<E, SELF> implements Iterable<E> {
    public AsmrListNode(AsmrNode<?> parent) {
        super(parent);
    }

    abstract E newElement();

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
