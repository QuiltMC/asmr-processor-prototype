package org.quiltmc.asmr.processor.tree;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class AsmrLdcInsnNode extends AsmrInsnNode<AsmrLdcInsnNode> {
    private final AsmrConstantList<?> cstList = new AsmrConstantList<>(this);

    private final List<AsmrNode<?>> children = Arrays.asList(visibleTypeAnnotations(), invisibleTypeAnnotations(),
            opcode(), cstList);

    public AsmrLdcInsnNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrLdcInsnNode newInstance(AsmrNode<?> parent) {
        return new AsmrLdcInsnNode(parent);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }

    public AsmrConstantList<?> cstList() {
        return cstList;
    }

    @Override
    void accept(MethodVisitor mv, ToIntFunction<AsmrIndex> localVariableIndexes, Function<AsmrIndex, Label> labelIndexes) {
        if (cstList.size() != 1) {
            throw new IllegalStateException("ldc constant list size not equal to 1");
        }
        mv.visitLdcInsn(AsmrInvokeDynamicInsnNode.toConstantArray(cstList)[0]);
    }
}
