package org.quiltmc.asmr.processor.capture;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.processor.AsmrProcessor;
import org.quiltmc.asmr.tree.AsmrAbstractListNode;
import org.quiltmc.asmr.tree.AsmrNode;

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
    public AsmrAbstractListNode<T, ?> resolvedList(AsmrProcessor processor) {
        return list;
    }

    @Override
    public int startIndexInclusive() {
        return 0;
    }

    @Override
    public int endIndexExclusive() {
        return list.size();
    }
}
