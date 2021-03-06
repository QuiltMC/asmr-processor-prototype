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

public class AsmrTypeInsnNode extends AsmrInsnNode<AsmrTypeInsnNode> {
    private final AsmrValueNode<String> desc = new AsmrValueNode<>(this);

    private final List<AsmrNode<?>> children = Arrays.asList(visibleTypeAnnotations(), invisibleTypeAnnotations(),
            opcode(), desc);

    public AsmrTypeInsnNode() {
        this(null);
    }

    @ApiStatus.Internal
    public AsmrTypeInsnNode(AsmrNode<?> parent) {
        super(parent);
    }

    @ApiStatus.Internal
    @Override
    public AsmrTypeInsnNode newInstance(AsmrNode<?> parent) {
        return new AsmrTypeInsnNode(parent);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }

    public AsmrValueNode<String> desc() {
        return desc;
    }

    @Override
    public void accept(MethodVisitor mv, ToIntFunction<AsmrIndex> localVariableIndexes,
                       Function<AsmrIndex, Label> labelIndexes) {
        mv.visitTypeInsn(opcode().value(), desc.value());
    }
}
