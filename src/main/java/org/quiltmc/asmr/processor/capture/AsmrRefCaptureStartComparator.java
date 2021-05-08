package org.quiltmc.asmr.processor.capture;

import org.jetbrains.annotations.ApiStatus;

import java.util.Comparator;

/**
 * Compares the {@code [...pathPrefix, startVirtualIndex]} of each capture by lexicographic order.
 */
@ApiStatus.Internal
public class AsmrRefCaptureStartComparator implements Comparator<AsmrReferenceCapture> {
    @Override
    public int compare(AsmrReferenceCapture captureA, AsmrReferenceCapture captureB) {
        int[] pathA = captureA.pathPrefix();
        int[] pathB = captureB.pathPrefix();
        int i;

        // find the point the two paths diverge, compare the children indexes at that point
        int minLength = Math.min(pathA.length, pathB.length);
        for (i = 0; i < minLength; i++) {
            if (pathA[i] != pathB[i]) {
                return Integer.compare(pathA[i], pathB[i]);
            }
        }

        if (pathA.length < pathB.length) {
            // pathA ended, so we need to check if b lies after the start index of a
            int aStartIndex = captureA.startIndexInclusive();
            return aStartIndex <= pathB[i] ? -1 : 1;
        } else if (pathB.length < pathA.length) {
            // pathB ended, so we need to check if a lies after the start index of b
            int bStartIndex = captureB.startIndexInclusive();
            return bStartIndex <= pathA[i] ? -1 : 1;
        } else { // if (pathA.length == pathB.length)
            // pathA and pathB both end at the same time, compare their start indexes
            int aStartVirtualIndex = captureA.startVirtualIndex();
            int bStartVirtualIndex = captureB.startVirtualIndex();
            return Integer.compare(aStartVirtualIndex, bStartVirtualIndex);
        }
    }
}
