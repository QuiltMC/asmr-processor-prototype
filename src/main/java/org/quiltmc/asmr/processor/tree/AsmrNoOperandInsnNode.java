package org.quiltmc.asmr.processor.tree;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class AsmrNoOperandInsnNode extends AsmrInsnNode<AsmrNoOperandInsnNode> {
    private final List<AsmrNode<?>> children = Arrays.asList(visibleTypeAnnotations(), invisibleTypeAnnotations(),
            opcode());

    public AsmrNoOperandInsnNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrNoOperandInsnNode newInstance(AsmrNode<?> parent) {
        return new AsmrNoOperandInsnNode(parent);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }

    @Override
    void accept(MethodVisitor mv, ToIntFunction<AsmrIndex> localVariableIndexes, Function<AsmrIndex, Label> labelIndexes) {
        mv.visitInsn(opcode().value());
    }
}
