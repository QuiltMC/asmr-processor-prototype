package org.quiltmc.asmr.processor.capture;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.processor.tree.AsmrAbstractListNode;
import org.quiltmc.asmr.processor.tree.AsmrNode;

@ApiStatus.Internal
public class AsmrCopySliceCapture<T extends AsmrNode<T>> implements AsmrSliceCapture<T> {
    private final AsmrAbstractListNode<T, ?> list;

    public AsmrCopySliceCapture(AsmrAbstractListNode<T, ?> list, int startInclusive, int endExclusive) {
        this.list = list.newInstance(null);
        for (int i = startInclusive; i < endExclusive; i++) {
            this.list.addCopy(list.get(i));
        }
    }

    @Override
    public AsmrAbstractListNode<T, ?> resolvedList() {
        return list;
    }

    @Override
    public int startNodeInclusive() {
        return 0;
    }

    @Override
    public int endNodeExclusive() {
        return list.size();
    }
}
