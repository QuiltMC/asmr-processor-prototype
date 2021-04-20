package org.quiltmc.asmr.processor.tree;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class AsmrIntInsnNode extends AsmrInsnNode<AsmrIntInsnNode> {
    private final AsmrValueNode<Integer> operand = new AsmrValueNode<>(this);

    private final List<AsmrNode<?>> children = Arrays.asList(visibleTypeAnnotations(), invisibleTypeAnnotations(),
            opcode(), operand);

    public AsmrIntInsnNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrIntInsnNode newInstance(AsmrNode<?> parent) {
        return new AsmrIntInsnNode(parent);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }

    public AsmrValueNode<Integer> operand() {
        return operand;
    }

    @Override
    void accept(MethodVisitor mv, ToIntFunction<AsmrIndex> localVariableIndexes, Function<AsmrIndex, Label> labelIndexes) {
        mv.visitIntInsn(opcode().value(), operand.value());
    }
}
