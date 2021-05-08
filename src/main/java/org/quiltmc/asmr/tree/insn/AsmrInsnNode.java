package org.quiltmc.asmr.tree.insn;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.tree.AsmrNode;
import org.quiltmc.asmr.tree.AsmrValueNode;
import org.quiltmc.asmr.tree.annotation.AsmrTypeAnnotationListNode;

public abstract class AsmrInsnNode<SELF extends AsmrInsnNode<SELF>> extends AsmrAbstractInsnNode<SELF> {
    private final AsmrTypeAnnotationListNode visibleTypeAnnotations = new AsmrTypeAnnotationListNode(this);
    private final AsmrTypeAnnotationListNode invisibleTypeAnnotations = new AsmrTypeAnnotationListNode(this);
    private final AsmrValueNode<Integer> opcode = new AsmrValueNode<>(this);

    public AsmrInsnNode() {
        this(null);
    }

    @ApiStatus.Internal
    public AsmrInsnNode(AsmrNode<?> parent) {
        super(parent);
    }

    public AsmrTypeAnnotationListNode visibleTypeAnnotations() {
        return visibleTypeAnnotations;
    }

    public AsmrTypeAnnotationListNode invisibleTypeAnnotations() {
        return invisibleTypeAnnotations;
    }

    public AsmrValueNode<Integer> opcode() {
        return opcode;
    }
}
