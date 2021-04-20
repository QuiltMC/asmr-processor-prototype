package org.quiltmc.asmr.processor.tree;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class AsmrVarInsnNode extends AsmrInsnNode<AsmrVarInsnNode> {
    private final AsmrValueNode<AsmrIndex> varIndex = new AsmrValueNode<>(this);

    private final List<AsmrNode<?>> children = Arrays.asList(visibleTypeAnnotations(), invisibleTypeAnnotations(),
            opcode(), varIndex);

    public AsmrVarInsnNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrVarInsnNode newInstance(AsmrNode<?> parent) {
        return new AsmrVarInsnNode(parent);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }

    public AsmrValueNode<AsmrIndex> varIndex() {
        return varIndex;
    }

    @Override
    void accept(MethodVisitor mv, ToIntFunction<AsmrIndex> localVariableIndexes, Function<AsmrIndex, Label> labelIndexes) {
        mv.visitVarInsn(opcode().value(), localVariableIndexes.applyAsInt(varIndex.value()));
    }
}
