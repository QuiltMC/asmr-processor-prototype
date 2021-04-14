package org.quiltmc.asmr.processor;

import org.quiltmc.asmr.processor.tree.AsmrNode;

public class AsmrStateManager {
    public static boolean isNodeWritable(AsmrNode<?> node) {
        // TODO: be smarter
        return true;
    }
}
