package org.quiltmc.asmr.processor.capture;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.processor.AsmrProcessor;

@ApiStatus.Internal
public interface AsmrReferenceCapture {
    String className();
    void computeResolved(AsmrProcessor processor);
}
