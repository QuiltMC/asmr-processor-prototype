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

public class AsmrLineNumberNode extends AsmrAbstractInsnNode<AsmrLineNumberNode> {
    private final AsmrValueNode<Integer> line = new AsmrValueNode<>(this);
    private final AsmrValueNode<AsmrIndex> start = new AsmrValueNode<>(this);

    private final List<AsmrNode<?>> children = Arrays.asList(line, start);

    public AsmrLineNumberNode() {
        this(null);
    }

    @ApiStatus.Internal
    public AsmrLineNumberNode(AsmrNode<?> parent) {
        super(parent);
    }

    @ApiStatus.Internal
    @Override
    public AsmrLineNumberNode newInstance(AsmrNode<?> parent) {
        return new AsmrLineNumberNode(parent);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }

    public AsmrValueNode<Integer> line() {
        return line;
    }

    public AsmrValueNode<AsmrIndex> start() {
        return start;
    }

    @Override
    public void accept(MethodVisitor mv, ToIntFunction<AsmrIndex> localVariableIndexes,
                       Function<AsmrIndex, Label> labelIndexes) {
        mv.visitLineNumber(line.value(), labelIndexes.apply(start.value()));
    }
}
