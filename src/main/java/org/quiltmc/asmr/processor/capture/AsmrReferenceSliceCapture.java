package org.quiltmc.asmr.processor.capture;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.processor.AsmrProcessor;
import org.quiltmc.asmr.processor.tree.AsmrAbstractListNode;
import org.quiltmc.asmr.processor.tree.AsmrNode;

@ApiStatus.Internal
public class AsmrReferenceSliceCapture<T extends AsmrNode<T>, L extends AsmrAbstractListNode<T, L>> implements AsmrSliceCapture<T>, AsmrReferenceCapture {
    private final AsmrReferenceNodeCapture<L> listCapture;
    private final int startIndex;
    private final int endIndex;
    private final boolean startInclusive;
    private final boolean endInclusive;
    private final boolean startIsAtStart;
    private final boolean endIsAtEnd;
    private T resolvedStart;
    private T resolvedEnd;
    private int resolvedStartIndex;
    private int resolvedEndIndex;

    @SuppressWarnings("unchecked")
    public AsmrReferenceSliceCapture(AsmrAbstractListNode<T, ?> list, int startIndex, int endIndex, boolean startInclusive, boolean endInclusive) {
        this.listCapture = new AsmrReferenceNodeCapture<>((L) list);
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.startInclusive = startInclusive;
        this.endInclusive = endInclusive;
        this.startIsAtStart = startIndex == -1 && !startInclusive;
        this.endIsAtEnd = endIndex == list.size() && !endInclusive;
    }

    @Override
    public String className() {
        return listCapture.className();
    }

    @Override
    public void computeResolved(AsmrProcessor processor) {
        listCapture.computeResolved(processor);
        L resolvedList = listCapture.resolved(processor);
        if (!endIsAtEnd && endIndex >= resolvedList.typedChildren().size()) {
            throw new IllegalStateException("Reference slice index out of bounds");
        }
        if (!startIsAtStart && (!endIsAtEnd || startIndex != endIndex)) {
            resolvedStart = resolvedList.typedChildren().get(startIndex);
        }
        if (!endIsAtEnd && (!startIsAtStart || endIndex != startIndex)) {
            resolvedEnd = resolvedList.typedChildren().get(endIndex);
        }
        resolvedStartIndex = startIndex;
        resolvedEndIndex = endIndex;
    }

    @Override
    public AsmrAbstractListNode<T, ?> resolvedList(AsmrProcessor processor) {
        return listCapture.resolved(processor);
    }

    @Override
    public int startNodeInclusive(AsmrProcessor processor) {
        L resolvedList = listCapture.resolved(processor);
        if (startIsAtStart) {
            return 0;
        } else if (endIsAtEnd && startIndex == endIndex) {
            return resolvedList.size();
        }
        resolvedStartIndex = find(resolvedList, resolvedStart, resolvedStartIndex);
        return startInclusive ? resolvedStartIndex : resolvedStartIndex + 1;
    }

    @Override
    public int endNodeExclusive(AsmrProcessor processor) {
        L resolvedList = listCapture.resolved(processor);
        if (endIsAtEnd) {
            return resolvedList.size();
        } else if (startIsAtStart && endIndex == startIndex) {
            return 0;
        }
        resolvedEndIndex = find(resolvedList, resolvedEnd, resolvedEndIndex);
        return endInclusive ? resolvedEndIndex + 1 : resolvedEndIndex;
    }

    private static <T extends AsmrNode<T>> int find(AsmrAbstractListNode<T, ?> list, T val, int startingIndex) {
        if (startingIndex >= list.size()) {
            startingIndex = list.size() - 1;
        }

        if (list.get(startingIndex) == val) {
            return startingIndex;
        }

        int distanceToStart = startingIndex;
        int distanceToEnd = list.size() - startingIndex - 1;
        for (int i = 1; i <= distanceToStart || i <= distanceToEnd; i++) {
            if (i <= distanceToStart) {
                T val2 = list.get(startingIndex - i);
                if (val2 == val) {
                    return startingIndex - i;
                }
            }
            if (i <= distanceToEnd) {
                T val2 = list.get(startingIndex + i);
                if (val2 == val) {
                    return startingIndex + i;
                }
            }
        }

        // TODO: better error message?
        throw new IllegalStateException("Reference slice start/end node is missing");
    }
}
