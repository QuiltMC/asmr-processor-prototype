package org.quiltmc.asmr.processor.tree.insn;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.quiltmc.asmr.processor.tree.AsmrNode;
import org.quiltmc.asmr.processor.tree.AsmrValueNode;
import org.quiltmc.asmr.processor.tree.method.AsmrIndex;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class AsmrTypeInsnNode extends AsmrInsnNode<AsmrTypeInsnNode> {
    private final AsmrValueNode<String> desc = new AsmrValueNode<>(this);

    private final List<AsmrNode<?>> children = Arrays.asList(visibleTypeAnnotations(), invisibleTypeAnnotations(),
            opcode(), desc);

    public AsmrTypeInsnNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    protected AsmrTypeInsnNode newInstance(AsmrNode<?> parent) {
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
