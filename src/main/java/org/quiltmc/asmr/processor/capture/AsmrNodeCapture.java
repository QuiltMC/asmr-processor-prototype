package org.quiltmc.asmr.processor.capture;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.processor.AsmrProcessor;
import org.quiltmc.asmr.processor.annotation.AllowLambdaCapture;
import org.quiltmc.asmr.tree.AsmrNode;

@AllowLambdaCapture
@ApiStatus.NonExtendable
public interface AsmrNodeCapture<T extends AsmrNode<T>> extends AsmrCapture {
    @ApiStatus.Internal
    T resolved(AsmrProcessor processor);
}
