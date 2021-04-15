package org.quiltmc.asmr.processor.tree;

public abstract class AsmrPolymorphicListNode<E extends AsmrNode<E>, SELF extends AsmrPolymorphicListNode<E, SELF>>
        extends AsmrAbstractListNode<E, SELF> {
    public AsmrPolymorphicListNode(AsmrNode<?> parent) {
        super(parent);
    }

    abstract E newElement(Class<? extends Type<? extends E>> type);

    public abstract Class<? extends Type<? extends E>> getType(E element);

    public E insert(int index, Class<? extends Type<? extends E>> type) {
        ensureWritable();
        E element = newElement(type);
        children.add(index, element);
        return element;
    }

    public E add(Class<? extends Type<? extends E>> type) {
        return insert(children.size(), type);
    }

    public Class<? extends Type<? extends E>> getType(int index) {
        return getType(get(index));
    }

    @SuppressWarnings("unchecked")
    public <T extends E> T get(int index, Class<? extends Type<? extends T>> type) {
        E val = get(index);
        if (getType(val) != type) {
            throw new ClassCastException("Wrong type");
        }
        return (T) val;
    }

    public interface Type<E> {
    }
}
