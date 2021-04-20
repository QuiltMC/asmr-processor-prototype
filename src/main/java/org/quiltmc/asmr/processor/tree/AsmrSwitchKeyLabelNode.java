package org.quiltmc.asmr.processor.tree;

import java.util.Arrays;
import java.util.List;

public class AsmrSwitchKeyLabelNode extends AsmrNode<AsmrSwitchKeyLabelNode> {
    private final AsmrValueNode<Integer> key = new AsmrValueNode<>(this);
    private final AsmrValueNode<AsmrIndex> label = new AsmrValueNode<>(this);

    private final List<AsmrNode<?>> children = Arrays.asList(key, label);

    public AsmrSwitchKeyLabelNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrSwitchKeyLabelNode newInstance(AsmrNode<?> parent) {
        return new AsmrSwitchKeyLabelNode(parent);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }

    public AsmrValueNode<Integer> key() {
        return key;
    }

    public AsmrValueNode<AsmrIndex> label() {
        return label;
    }
}
