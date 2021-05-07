package org.quiltmc.asmr.processor.capture;

import java.util.Comparator;

public class AsmrReferenceCaptureComparator implements Comparator<AsmrReferenceCapture> {
    @Override
    public int compare(AsmrReferenceCapture captureA, AsmrReferenceCapture captureB) {
        int[] pathA = captureA.pathPrefix();
        int[] pathB = captureB.pathPrefix();
        int i;
        // find the point the two paths diverge, compare the children indexes at that point
        for (i = 0; i < pathA.length && i < pathB.length; i++) {
            if (pathA[i] != pathB[i]) {
                return Integer.compare(pathA[i], pathB[i]);
            }
        }

        if (pathA.length < pathB.length && captureA instanceof AsmrReferenceSliceCapture) {
            // pathA ended but captureA is a slice, so need to check the slice index
            int aStartIndex = ((AsmrReferenceSliceCapture<?, ?>) captureA).startIndexInclusive();
            return aStartIndex <= pathB[i] ? -1 : 1;
        } else if (pathB.length < pathA.length && captureB instanceof AsmrReferenceSliceCapture) {
            // pathB ended but captureB is a slice, so need to check the slice index
            int bStartIndex = ((AsmrReferenceSliceCapture<?, ?>) captureB).startIndexInclusive();
            return bStartIndex <= pathA[i] ? -1 : 1;
        } else if (pathA.length == pathB.length && captureA instanceof AsmrReferenceSliceCapture && captureB instanceof AsmrReferenceSliceCapture) {
            // pathA and pathB both end at the same time and are slices, compare their start indexes
            int aStartVirtualIndex = ((AsmrReferenceSliceCapture<?, ?>) captureA).startVirtualIndex();
            int bStartVirtualIndex = ((AsmrReferenceSliceCapture<?, ?>) captureB).startVirtualIndex();
            return Integer.compare(aStartVirtualIndex, bStartVirtualIndex);
        } else {
            // sort the outer one before the inner one
            return Integer.compare(pathA.length, pathB.length);
        }
    }
}