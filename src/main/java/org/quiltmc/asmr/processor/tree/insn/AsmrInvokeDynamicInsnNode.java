package org.quiltmc.asmr.processor.tree.insn;

import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.quiltmc.asmr.processor.tree.AsmrNode;
import org.quiltmc.asmr.processor.tree.AsmrPolymorphicListNode;
import org.quiltmc.asmr.processor.tree.AsmrValueNode;
import org.quiltmc.asmr.processor.tree.method.AsmrIndex;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class AsmrInvokeDynamicInsnNode extends AsmrInsnNode<AsmrInvokeDynamicInsnNode> {
    private final AsmrValueNode<String> name = new AsmrValueNode<>(this);
    private final AsmrValueNode<String> desc = new AsmrValueNode<>(this);
    private final AsmrHandleNode bsm = new AsmrHandleNode(this);
    private final AsmrConstantList<?> bsmArgs = new AsmrConstantList<>(this);

    private final List<AsmrNode<?>> children = Arrays.asList(visibleTypeAnnotations(), invisibleTypeAnnotations(),
            opcode(), name, desc, bsm, bsmArgs);

    public AsmrInvokeDynamicInsnNode() {
        this(null);
    }

    @ApiStatus.Internal
    public AsmrInvokeDynamicInsnNode(AsmrNode<?> parent) {
        super(parent);
    }

    @ApiStatus.Internal
    @Override
    public AsmrInvokeDynamicInsnNode newInstance(AsmrNode<?> parent) {
        return new AsmrInvokeDynamicInsnNode(parent);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }

    public AsmrValueNode<String> name() {
        return name;
    }

    public AsmrValueNode<String> desc() {
        return desc;
    }

    public AsmrHandleNode bsm() {
        return bsm;
    }

    public AsmrConstantList<?> bsmArgs() {
        return bsmArgs;
    }

    @Override
    public void accept(MethodVisitor mv, ToIntFunction<AsmrIndex> localVariableIndexes,
                       Function<AsmrIndex, Label> labelIndexes) {
        mv.visitInvokeDynamicInsn(name.value(), desc.value(), toHandle(bsm), toConstantArray(bsmArgs));
    }

    private static Handle toHandle(AsmrHandleNode asmrHandle) {
        return new Handle(
                asmrHandle.tag().value().value(),
                asmrHandle.owner().value(),
                asmrHandle.name().value(),
                asmrHandle.desc().value(),
                asmrHandle.itf().value()
        );
    }

    static Object[] toConstantArray(AsmrConstantList<?> constantList) {
        Object[] array = new Object[constantList.size()];
        for (int i = 0; i < array.length; i++) {
            AsmrPolymorphicListNode.Type<?> type = constantList.getType(i);
            if (type == AsmrConstantList.HandleType.INSTANCE) {
                array[i] = toHandle(constantList.get(i, AsmrConstantList.HandleType.INSTANCE));
            } else if (type == AsmrConstantList.ConstantDynamicType.INSTANCE) {
                AsmrConstantDynamicNode constantDynamic = constantList.get(i, AsmrConstantList.ConstantDynamicType.INSTANCE);
                array[i] = new ConstantDynamic(
                        constantDynamic.name().value(),
                        constantDynamic.desc().value(),
                        toHandle(constantDynamic.bsm()),
                        toConstantArray(constantDynamic.bsmArgs())
                );
            } else {
                array[i] = ((AsmrValueNode<?>) constantList.get(i)).value();
            }
        }
        return array;
    }
}
