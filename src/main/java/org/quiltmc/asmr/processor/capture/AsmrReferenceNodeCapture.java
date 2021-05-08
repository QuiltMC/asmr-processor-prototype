package org.quiltmc.asmr.processor.capture;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.processor.AsmrProcessor;
import org.quiltmc.asmr.tree.AsmrNode;
import org.quiltmc.asmr.tree.member.AsmrClassNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ApiStatus.Internal
public class AsmrReferenceNodeCapture<T extends AsmrNode<T>> implements AsmrNodeCapture<T>, AsmrReferenceCapture {
    private final Class<? extends T> type;
    private final String className;
    private final int[] path;
    private T cachedResolved = null;

    @SuppressWarnings("unchecked")
    public AsmrReferenceNodeCapture(AsmrProcessor processor, T node) {
        this.type = (Class<? extends T>) node.getClass();

        List<Integer> reversedIndexes = new ArrayList<>();
        AsmrNode<?> n = node, parent;
        while ((parent = n.parent()) != null) {
            // TODO: something more efficient than linear search?
            reversedIndexes.add(parent.children().indexOf(n));
            n = parent;
        }
        this.path = new int[reversedIndexes.size()];
        for (int i = 0; i < this.path.length; i++) {
            this.path[i] = reversedIndexes.get(this.path.length - 1 - i);
        }

        if (!(n instanceof AsmrClassNode)) {
            throw new IllegalArgumentException("Cannot reference capture a node that does not descend from a class node");
        }
        this.className = ((AsmrClassNode) n).name().value();
        if (processor.findClassImmediately(className) != n) {
            throw new IllegalArgumentException("Cannot reference capture a node in a class that doesn't exist");
        }
    }

    @Override
    public String className() {
        return className;
    }

    @Override
    public int[] pathPrefix() {
        return path;
    }

    @Override
    public T resolved(AsmrProcessor processor) {
        processor.checkWritingClass(className);

        if (cachedResolved != null) {
            return cachedResolved;
        }

        AsmrNode<?> node = processor.findClassImmediately(className);
        if (node == null) {
            throw new IllegalStateException("Reference capture to class '" + className + "' which does not exist in this processor");
        }

        for (int childIndex : path) {
            if (childIndex >= node.children().size()) {
                throw new IllegalStateException("Invalid path in class '" + className + "': " + Arrays.toString(path));
            }
            node = node.children().get(childIndex);
        }

        return cachedResolved = type.cast(node);
    }

    @Override
    public String toString() {
        return "&" + className + "::" + Arrays.toString(path);
    }
}
