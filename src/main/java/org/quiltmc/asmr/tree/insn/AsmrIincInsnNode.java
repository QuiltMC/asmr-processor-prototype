package org.quiltmc.asmr.tree.insn;

import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.quiltmc.asmr.tree.AsmrNode;
import org.quiltmc.asmr.tree.AsmrValueNode;
import org.quiltmc.asmr.tree.method.AsmrIndex;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class AsmrIincInsnNode extends AsmrInsnNode<AsmrIincInsnNode> {
    private final AsmrValueNode<AsmrIndex> var = new AsmrValueNode<>(this);
    private final AsmrValueNode<Integer> incr = new AsmrValueNode<>(this);

    private final List<AsmrNode<?>> children = Arrays.asList(visibleTypeAnnotations(), invisibleTypeAnnotations(),
            opcode(), var, incr);

    public AsmrIincInsnNode() {
        this(null);
    }

    @ApiStatus.Internal
    public AsmrIincInsnNode(AsmrNode<?> parent) {
        super(parent);
    }

    @ApiStatus.Internal
    @Override
    public AsmrIincInsnNode newInstance(AsmrNode<?> parent) {
        return new AsmrIincInsnNode(parent);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }

    public AsmrValueNode<AsmrIndex> var() {
        return var;
    }

    public AsmrValueNode<Integer> incr() {
        return incr;
    }

    @Override
    public void accept(MethodVisitor mv, ToIntFunction<AsmrIndex> localVariableIndexes,
                       Function<AsmrIndex, Label> labelIndexes) {
        mv.visitIincInsn(localVariableIndexes.applyAsInt(var.value()), incr.value());
    }
}
