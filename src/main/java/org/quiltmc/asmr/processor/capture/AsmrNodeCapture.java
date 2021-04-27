package org.quiltmc.asmr.processor.capture;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.processor.AsmrProcessor;
import org.quiltmc.asmr.processor.annotation.AllowLambdaCapture;
import org.quiltmc.asmr.processor.tree.AsmrNode;

@AllowLambdaCapture
@ApiStatus.NonExtendable
public interface AsmrNodeCapture<T extends AsmrNode<T>> {
    @ApiStatus.Internal
    T resolved(AsmrProcessor processor);
}
