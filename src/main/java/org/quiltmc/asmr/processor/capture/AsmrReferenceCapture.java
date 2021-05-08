package org.quiltmc.asmr.processor.capture;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface AsmrReferenceCapture {
    String className();
    int[] pathPrefix();

    int startVirtualIndex();
    int endVirtualIndex();
    int startIndexInclusive();
    int endIndexExclusive();
}
