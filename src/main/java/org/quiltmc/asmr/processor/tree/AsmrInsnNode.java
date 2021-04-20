package org.quiltmc.asmr.processor.tree;

public abstract class AsmrInsnNode<SELF extends AsmrInsnNode<SELF>> extends AsmrAbstractInsnNode<SELF> {
    private final AsmrTypeAnnotationListNode visibleTypeAnnotations = new AsmrTypeAnnotationListNode(this);
    private final AsmrTypeAnnotationListNode invisibleTypeAnnotations = new AsmrTypeAnnotationListNode(this);
    private final AsmrValueNode<Integer> opcode = new AsmrValueNode<>(this);

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
