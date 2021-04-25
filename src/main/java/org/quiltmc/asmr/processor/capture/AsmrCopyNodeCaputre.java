package org.quiltmc.asmr.processor.capture;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.processor.tree.AsmrNode;

@ApiStatus.Internal
public class AsmrCopyNodeCaputre<T extends AsmrNode<T>> implements AsmrNodeCapture<T> {
    private final T value;

    public AsmrCopyNodeCaputre(T value) {
        this.value = value.copy(null);
    }

    @Override
    public T resolved() {
        return value;
    }
}
