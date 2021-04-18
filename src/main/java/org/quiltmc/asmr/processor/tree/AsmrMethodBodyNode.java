package org.quiltmc.asmr.processor.tree;

import java.util.Arrays;
import java.util.List;

public class AsmrMethodBodyNode extends AsmrNode<AsmrMethodBodyNode> {
    private final List<AsmrNode<?>> children = Arrays.asList();

    public AsmrMethodBodyNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrMethodBodyNode newInstance(AsmrNode<?> parent) {
        return new AsmrMethodBodyNode(parent);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }
}
