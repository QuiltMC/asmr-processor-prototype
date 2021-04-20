package org.quiltmc.asmr.processor.tree.insn;

import org.quiltmc.asmr.processor.tree.AsmrNode;
import org.quiltmc.asmr.processor.tree.AsmrValueNode;

import java.util.Arrays;
import java.util.List;

public class AsmrConstantDynamicNode extends AsmrNode<AsmrConstantDynamicNode> {
    private final AsmrValueNode<String> name = new AsmrValueNode<>(this);
    private final AsmrValueNode<String> desc = new AsmrValueNode<>(this);
    private final AsmrHandleNode bsm = new AsmrHandleNode(this);
    private final AsmrConstantList<?> bsmArgs = new AsmrConstantList<>(this);

    private final List<AsmrNode<?>> children = Arrays.asList(name, desc, bsm, bsmArgs);

    public AsmrConstantDynamicNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    protected AsmrConstantDynamicNode newInstance(AsmrNode<?> parent) {
        return new AsmrConstantDynamicNode(parent);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }

    public AsmrValueNode<String> name() {
        return name;
    }

    public AsmrValueNode<String> desc() {
        return desc;
    }

    public AsmrHandleNode bsm() {
        return bsm;
    }

    public AsmrConstantList<?> bsmArgs() {
        return bsmArgs;
    }
}
