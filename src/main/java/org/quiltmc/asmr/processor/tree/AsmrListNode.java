package org.quiltmc.asmr.processor.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public abstract class AsmrListNode<E extends AsmrNode<E>, SELF extends AsmrListNode<E, SELF>> extends AsmrNode<SELF> implements Iterable<E> {
    private final List<E> children = new ArrayList<>();

    public AsmrListNode(AsmrNode<?> parent) {
        super(parent);
    }

    public List<E> typedChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return Collections.unmodifiableList(children);
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

    public E insertCopy(int index, E element) {
        ensureWritable();
        E newElement = element.copy(this);
        children.add(index, newElement);
        return newElement;
    }

    public E addCopy(E element) {
        return insertCopy(children.size(), element);
    }

    public void remove(int index) {
        children.remove(index);
    }

    public void remove(int start, int end) {
        children.subList(start, end).clear();
    }

    public E get(int index) {
        return children.get(index);
    }

    public int size() {
        return children.size();
    }

    @Override
    void copyFrom(SELF other) {
        children.clear();
        for (E e : other) {
            addCopy(e);
        }
    }

    @Override
    public Iterator<E> iterator() {
        return Collections.unmodifiableList(children).iterator();
    }

    public Stream<E> stream() {
        return children.stream();
    }
}
