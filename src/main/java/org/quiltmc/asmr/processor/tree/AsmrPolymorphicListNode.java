package org.quiltmc.asmr.processor.tree;

public abstract class AsmrPolymorphicListNode<E extends AsmrNode<E>, SELF extends AsmrPolymorphicListNode<E, SELF>>
        extends AsmrAbstractListNode<E, SELF> {
    public AsmrPolymorphicListNode(AsmrNode<?> parent) {
        super(parent);
    }

    protected abstract E newElement(Type<?> type);

    public abstract Type<? extends E> getType(E element);

    // This generic type T loses the bound T extends E, but Java is stupid and you can't express that without breaking
    // things. Because of the implementation with newElement, any attempt to pass in a Type<T> where T extends E isn't
    // satisfied will still throw a runtime error. It sucks that this can't be a compile time error. If you find a
    // solution to this problem that actually compiles, email me at earthcomputer@ireallyhatejava.com as a matter of
    // urgency.
    // @TODO: something to fix after prototype
    @SuppressWarnings("unchecked")
    public <T extends AsmrNode<T>> T insert(int index, Type<T> type) {
        ensureWritable();
        E element = newElement(type);
        children.add(index, element);
        return (T) element;
    }

    public <T extends AsmrNode<T>> T add(Type<T> type) {
        return insert(children.size(), type);
    }

    public Type<? extends E> getType(int index) {
        return getType(get(index));
    }

    @SuppressWarnings("unchecked")
    public <T extends AsmrNode<T>> T get(int index, Type<T> type) {
        E val = get(index);
        if (getType(val) != type) {
            throw new ClassCastException("Wrong type");
        }
        return (T) val;
    }

    public interface Type<E> {
    }
}
