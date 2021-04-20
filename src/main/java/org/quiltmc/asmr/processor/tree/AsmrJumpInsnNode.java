package org.quiltmc.asmr.processor.tree;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class AsmrJumpInsnNode extends AsmrInsnNode<AsmrJumpInsnNode> {
    private final AsmrValueNode<AsmrIndex> label = new AsmrValueNode<>(this);

    private final List<AsmrNode<?>> children = Arrays.asList(visibleTypeAnnotations(), invisibleTypeAnnotations(),
            opcode(), label);

    public AsmrJumpInsnNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrJumpInsnNode newInstance(AsmrNode<?> parent) {
        return new AsmrJumpInsnNode(parent);
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
        mv.visitJumpInsn(opcode().value(), labelIndexes.apply(label.value()));
    }
}
