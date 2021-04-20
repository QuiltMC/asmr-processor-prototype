package org.quiltmc.asmr.processor.tree;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.function.Function;
import java.util.function.ToIntFunction;

public abstract class AsmrAbstractInsnNode<SELF extends AsmrAbstractInsnNode<SELF>> extends AsmrNode<SELF> {
    public AsmrAbstractInsnNode(AsmrNode<?> parent) {
        super(parent);
    }

    abstract void accept(MethodVisitor mv, ToIntFunction<AsmrIndex> localVariableIndexes, Function<AsmrIndex, Label> labelIndexes);
}
