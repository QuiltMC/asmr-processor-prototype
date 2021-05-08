package org.quiltmc.asmr.processor.capture;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.processor.AsmrProcessor;
import org.quiltmc.asmr.tree.AsmrAbstractListNode;
import org.quiltmc.asmr.tree.AsmrNode;

@ApiStatus.Internal
public class AsmrReferenceSliceCapture<T extends AsmrNode<T>, L extends AsmrAbstractListNode<T, L>> implements AsmrSliceCapture<T>, AsmrReferenceCapture {
    private final AsmrReferenceNodeCapture<L> listCapture;
    private int startVirtualIndex;
    private int endVirtualIndex;

    @SuppressWarnings("unchecked")
    public AsmrReferenceSliceCapture(AsmrProcessor processor, AsmrAbstractListNode<T, ?> list, int startVirtualIndex, int endVirtualIndex) {
        this.listCapture = new AsmrReferenceNodeCapture<>(processor, (L) list);
        this.startVirtualIndex = startVirtualIndex;
        this.endVirtualIndex = endVirtualIndex;
    }

    @Override
    public String className() {
        return listCapture.className();
    }

    @Override
    public int[] pathPrefix() {
        return listCapture.pathPrefix();
    }

    @Override
    public AsmrAbstractListNode<T, ?> resolvedList(AsmrProcessor processor) {
        return listCapture.resolved(processor);
    }

    @Override
    public int startVirtualIndex() {
        return startVirtualIndex;
    }

    @Override
    public int endVirtualIndex() {
        return endVirtualIndex;
    }

    @Override
    public int startIndexInclusive() {
        return startVirtualIndex / 2;
    }

    @Override
    public int endIndexExclusive() {
        return endVirtualIndex / 2;
    }

    public void shiftStartVirtualIndex(int shiftBy) {
        startVirtualIndex += shiftBy;
    }

    public void shiftEndVirtualIndex(int shiftBy) {
        endVirtualIndex += shiftBy;
    }
}
