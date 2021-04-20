package org.quiltmc.asmr.processor.tree;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class AsmrIincInsnNode extends AsmrInsnNode<AsmrIincInsnNode> {
    private final AsmrValueNode<AsmrIndex> var = new AsmrValueNode<>(this);
    private final AsmrValueNode<Integer> incr = new AsmrValueNode<>(this);

    private final List<AsmrNode<?>> children = Arrays.asList(visibleTypeAnnotations(), invisibleTypeAnnotations(),
            opcode(), var, incr);

    public AsmrIincInsnNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrIincInsnNode newInstance(AsmrNode<?> parent) {
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
    void accept(MethodVisitor mv, ToIntFunction<AsmrIndex> localVariableIndexes, Function<AsmrIndex, Label> labelIndexes) {
        mv.visitIincInsn(localVariableIndexes.applyAsInt(var.value()), incr.value());
    }
}
