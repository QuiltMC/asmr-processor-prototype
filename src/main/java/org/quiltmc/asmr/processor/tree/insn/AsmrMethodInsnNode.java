package org.quiltmc.asmr.processor.tree.insn;

import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.quiltmc.asmr.processor.tree.AsmrNode;
import org.quiltmc.asmr.processor.tree.AsmrValueNode;
import org.quiltmc.asmr.processor.tree.method.AsmrIndex;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class AsmrMethodInsnNode extends AsmrInsnNode<AsmrMethodInsnNode> {
    private final AsmrValueNode<String> owner = new AsmrValueNode<>(this);
    private final AsmrValueNode<String> name = new AsmrValueNode<>(this);
    private final AsmrValueNode<String> desc = new AsmrValueNode<>(this);
    private final AsmrValueNode<Boolean> itf = new AsmrValueNode<>(this);

    private final List<AsmrNode<?>> children = Arrays.asList(visibleTypeAnnotations(), invisibleTypeAnnotations(),
            opcode(), owner, name, desc, itf);

    public AsmrMethodInsnNode() {
        this(null);
    }

    @ApiStatus.Internal
    public AsmrMethodInsnNode(AsmrNode<?> parent) {
        super(parent);
    }

    @ApiStatus.Internal
    @Override
    public AsmrMethodInsnNode newInstance(AsmrNode<?> parent) {
        return new AsmrMethodInsnNode(parent);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }

    public AsmrValueNode<String> owner() {
        return owner;
    }

    public AsmrValueNode<String> name() {
        return name;
    }

    public AsmrValueNode<String> desc() {
        return desc;
    }

    public AsmrValueNode<Boolean> itf() {
        return itf;
    }

    @Override
    public void accept(MethodVisitor mv, ToIntFunction<AsmrIndex> localVariableIndexes,
                       Function<AsmrIndex, Label> labelIndexes) {
        mv.visitMethodInsn(opcode().value(), owner.value(), name.value(), desc.value(), itf.value());
    }
}
