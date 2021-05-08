package org.quiltmc.asmr.tree;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class AsmrTreeModificationManager {
    private AsmrTreeModificationManager() {}

    private static final ThreadLocal<Boolean> modificationTemporarilyAllowed = ThreadLocal.withInitial(() -> true);

    public static void enableModification() {
        modificationTemporarilyAllowed.set(true);
    }

    public static void disableModification() {
        modificationTemporarilyAllowed.set(false);
    }

    public static boolean isModificationEnabled() {
        return modificationTemporarilyAllowed.get();
    }
}
