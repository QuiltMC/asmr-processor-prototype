package org.quiltmc.asmr.processor.tree;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class AsmrLabelNode extends AsmrAbstractInsnNode<AsmrLabelNode> {
    private final AsmrValueNode<AsmrIndex> label = new AsmrValueNode<>(this);

    private final List<AsmrNode<?>> children = Collections.singletonList(label);

    public AsmrLabelNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrLabelNode newInstance(AsmrNode<?> parent) {
        return new AsmrLabelNode(parent);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }

    public AsmrValueNode<AsmrIndex> label() {
        return label;
    }

    @Override
    void accept(MethodVisitor mv, ToIntFunction<AsmrIndex> localVariableIndexes, Function<AsmrIndex, Label> labelIndexes) {
        mv.visitLabel(labelIndexes.apply(label.value()));
    }
}
