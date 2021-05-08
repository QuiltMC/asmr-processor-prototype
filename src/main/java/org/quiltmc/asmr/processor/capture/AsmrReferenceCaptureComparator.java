package org.quiltmc.asmr.processor.capture;

import java.util.Comparator;

public class AsmrReferenceCaptureComparator implements Comparator<AsmrReferenceCapture> {
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
            // pathA ended but captureA is a slice, so need to check the slice index
            int aStartIndex = captureA.startIndexInclusive();
            return aStartIndex <= pathB[i] ? -1 : 1;
        } else if (pathB.length < pathA.length) {
            // pathB ended but captureB is a slice, so need to check the slice index
            int bStartIndex = captureB.startIndexInclusive();
            return bStartIndex <= pathA[i] ? -1 : 1;
        } else {
            // pathA and pathB both end at the same time, compare the start indexes, if tied sort by last index
            int aStartVirtualIndex = captureA.startVirtualIndex();
            int bStartVirtualIndex = captureB.startVirtualIndex();
            if(aStartVirtualIndex == bStartVirtualIndex){
                return Integer.compare(captureA.endVirtualIndex(), bStartVirtualIndex);
            }
            return Integer.compare(aStartVirtualIndex, bStartVirtualIndex);
        }
    }
}