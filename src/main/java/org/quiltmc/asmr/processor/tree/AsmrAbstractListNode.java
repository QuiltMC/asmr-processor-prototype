package org.quiltmc.asmr.processor.tree;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public abstract class AsmrAbstractListNode<E extends AsmrNode<E>, SELF extends AsmrAbstractListNode<E, SELF>> extends AsmrNode<SELF> implements Iterable<E> {
    final List<E> children = new ArrayList<>();

    public AsmrAbstractListNode() {
        this(null);
    }

    @ApiStatus.Internal
    public AsmrAbstractListNode(AsmrNode<?> parent) {
        super(parent);
    }

    public List<E> typedChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return Collections.unmodifiableList(children);
    }

    public E insertCopy(int index, E element) {
        ensureWritable();
        E newElement = element.copy(this);
        children.add(index, newElement);
        return newElement;
    }

    @SuppressWarnings("unchecked")
    public void insertCopy(int index, AsmrAbstractListNode<? extends E, ?> other) {
        for (E e : other) {
            insertCopy(index++, e);
        }
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

    public boolean isEmpty() {
        return children.isEmpty();
    }

    @ApiStatus.Internal
    @Override
    public void copyFrom(SELF other) {
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
