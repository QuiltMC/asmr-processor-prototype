package org.quiltmc.asmr.processor.tree.method;

import org.quiltmc.asmr.processor.tree.AsmrNode;
import org.quiltmc.asmr.processor.tree.AsmrValueNode;
import org.quiltmc.asmr.processor.tree.annotation.AsmrTypeAnnotationListNode;

import java.util.Arrays;
import java.util.List;

public class AsmrTryCatchBlockNode extends AsmrNode<AsmrTryCatchBlockNode> {
    private final AsmrValueNode<AsmrIndex> start = new AsmrValueNode<>(this);
    private final AsmrValueNode<AsmrIndex> end = new AsmrValueNode<>(this);
    private final AsmrValueNode<AsmrIndex> handler = new AsmrValueNode<>(this);
    private final AsmrValueNode<String> type = new AsmrValueNode<>(this);
    private final AsmrTypeAnnotationListNode visibleTypeAnnotations = new AsmrTypeAnnotationListNode(this);
    private final AsmrTypeAnnotationListNode invisibleTypeAnnotations = new AsmrTypeAnnotationListNode(this);

    private final List<AsmrNode<?>> children = Arrays.asList(start, end, handler, type, visibleTypeAnnotations, invisibleTypeAnnotations);

    public AsmrTryCatchBlockNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    protected AsmrTryCatchBlockNode newInstance(AsmrNode<?> parent) {
        return null;
    }

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }

    public AsmrValueNode<AsmrIndex> start() {
        return start;
    }

    public AsmrValueNode<AsmrIndex> end() {
        return end;
    }

    public AsmrValueNode<AsmrIndex> handler() {
        return handler;
    }

    public AsmrValueNode<String> type() {
        return type;
    }

    public AsmrTypeAnnotationListNode visibleTypeAnnotations() {
        return visibleTypeAnnotations;
    }

    public AsmrTypeAnnotationListNode invisibleTypeAnnotations() {
        return invisibleTypeAnnotations;
    }
}
