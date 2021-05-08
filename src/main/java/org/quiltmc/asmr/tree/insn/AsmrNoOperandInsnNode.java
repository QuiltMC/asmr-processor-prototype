package org.quiltmc.asmr.tree.insn;

import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.quiltmc.asmr.tree.AsmrNode;
import org.quiltmc.asmr.tree.method.AsmrIndex;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class AsmrNoOperandInsnNode extends AsmrInsnNode<AsmrNoOperandInsnNode> {
    private final List<AsmrNode<?>> children = Arrays.asList(visibleTypeAnnotations(), invisibleTypeAnnotations(),
            opcode());

    public AsmrNoOperandInsnNode() {
        this(null);
    }

    @ApiStatus.Internal
    public AsmrNoOperandInsnNode(AsmrNode<?> parent) {
        super(parent);
    }

    @ApiStatus.Internal
    @Override
    public AsmrNoOperandInsnNode newInstance(AsmrNode<?> parent) {
        return new AsmrNoOperandInsnNode(parent);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }

    @Override
    public void accept(MethodVisitor mv, ToIntFunction<AsmrIndex> localVariableIndexes,
                       Function<AsmrIndex, Label> labelIndexes) {
        mv.visitInsn(opcode().value());
    }
}
