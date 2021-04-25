package org.quiltmc.asmr.processor.capture;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.processor.tree.AsmrAbstractListNode;
import org.quiltmc.asmr.processor.tree.AsmrNode;

@AllowLambdaCapture
@ApiStatus.NonExtendable
public interface AsmrSliceCapture<T extends AsmrNode<T>> {
    @ApiStatus.Internal
    AsmrAbstractListNode<T, ?> resolvedList();

    @ApiStatus.Internal
    int startNodeInclusive();

    @ApiStatus.Internal
    int endNodeExclusive();
}
