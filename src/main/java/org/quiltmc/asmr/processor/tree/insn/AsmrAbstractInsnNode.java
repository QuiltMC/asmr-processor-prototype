package org.quiltmc.asmr.processor.tree.insn;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.quiltmc.asmr.processor.tree.AsmrNode;
import org.quiltmc.asmr.processor.tree.method.AsmrIndex;

import java.util.function.Function;
import java.util.function.ToIntFunction;

public abstract class AsmrAbstractInsnNode<SELF extends AsmrAbstractInsnNode<SELF>> extends AsmrNode<SELF> {
    public AsmrAbstractInsnNode(AsmrNode<?> parent) {
        super(parent);
    }

    public abstract void accept(MethodVisitor mv, ToIntFunction<AsmrIndex> localVariableIndexes, Function<AsmrIndex,
            Label> labelIndexes);
}
