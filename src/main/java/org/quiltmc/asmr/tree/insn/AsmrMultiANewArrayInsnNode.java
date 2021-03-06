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

public class AsmrMultiANewArrayInsnNode extends AsmrInsnNode<AsmrMultiANewArrayInsnNode> {
    private final AsmrValueNode<String> desc = new AsmrValueNode<>(this);
    private final AsmrValueNode<Integer> dims = new AsmrValueNode<>(this);

    private final List<AsmrNode<?>> children = Arrays.asList(visibleTypeAnnotations(), invisibleTypeAnnotations(),
            opcode(), desc, dims);

    public AsmrMultiANewArrayInsnNode() {
        this(null);
    }

    @ApiStatus.Internal
    public AsmrMultiANewArrayInsnNode(AsmrNode<?> parent) {
        super(parent);
    }

    @ApiStatus.Internal
    @Override
    public AsmrMultiANewArrayInsnNode newInstance(AsmrNode<?> parent) {
        return new AsmrMultiANewArrayInsnNode(parent);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }

    public AsmrValueNode<String> desc() {
        return desc;
    }

    public AsmrValueNode<Integer> dims() {
        return dims;
    }

    @Override
    public void accept(MethodVisitor mv, ToIntFunction<AsmrIndex> localVariableIndexes,
                       Function<AsmrIndex, Label> labelIndexes) {
        mv.visitMultiANewArrayInsn(desc.value(), dims.value());
    }
}
