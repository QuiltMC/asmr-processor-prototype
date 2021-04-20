package org.quiltmc.asmr.processor.tree;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.TypePath;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class AsmrMethodBodyNode extends AsmrNode<AsmrMethodBodyNode> {
    private final AsmrValueListNode<AsmrIndex> localIndexes = new AsmrValueListNode<>(this);
    private final AsmrInstructionList<?> instructions = new AsmrInstructionList<>(this);
    private final AsmrTryCatchBlockListNode tryCatchBlocks = new AsmrTryCatchBlockListNode(this);
    private final AsmrLocalVariableListNode localVariables = new AsmrLocalVariableListNode(this);
    private final AsmrLocalVariableAnnotationListNode visibleLocalVariableAnnotations = new AsmrLocalVariableAnnotationListNode(this);
    private final AsmrLocalVariableAnnotationListNode invisibleLocalVariableAnnotations = new AsmrLocalVariableAnnotationListNode(this);

    private final List<AsmrNode<?>> children = Arrays.asList(localIndexes, instructions, tryCatchBlocks, localVariables,
            visibleLocalVariableAnnotations, invisibleLocalVariableAnnotations);

    public AsmrMethodBodyNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrMethodBodyNode newInstance(AsmrNode<?> parent) {
        return new AsmrMethodBodyNode(parent);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }

    public AsmrValueListNode<AsmrIndex> localIndexes() {
        return localIndexes;
    }

    public AsmrInstructionList<?> instructions() {
        return instructions;
    }

    public AsmrTryCatchBlockListNode tryCatchBlocks() {
        return tryCatchBlocks;
    }

    public AsmrLocalVariableListNode localVariables() {
        return localVariables;
    }

    public AsmrLocalVariableAnnotationListNode visibleLocalVariableAnnotations() {
        return visibleLocalVariableAnnotations;
    }

    public AsmrLocalVariableAnnotationListNode invisibleLocalVariableAnnotations() {
        return invisibleLocalVariableAnnotations;
    }

    public void accept(MethodVisitor mv) {
        Map<AsmrIndex, Label> labelIndexes = new HashMap<>();
        Function<AsmrIndex, Label> labelFunction = index -> labelIndexes.computeIfAbsent(index, k -> new Label());
        Map<AsmrIndex, Integer> localVariableIndexes = new HashMap<>(this.localIndexes.size());
        for (int i = 0; i < this.localIndexes.size(); i++) {
            localVariableIndexes.put(this.localIndexes.get(i).value(), i);
        }
        ToIntFunction<AsmrIndex> localResolver = index -> {
            Integer val = localVariableIndexes.get(index);
            if (val == null) {
                throw new IllegalStateException("Use of unregistered local variable " + index);
            }
            return val;
        };

        if (!instructions.isEmpty()) {
            mv.visitCode();

            for (AsmrTryCatchBlockNode tryCatchBlock : tryCatchBlocks) {
                mv.visitTryCatchBlock(
                        labelFunction.apply(tryCatchBlock.start().value()),
                        labelFunction.apply(tryCatchBlock.end().value()),
                        labelFunction.apply(tryCatchBlock.handler().value()),
                        AsmrTreeUtil.toNullableString(tryCatchBlock.type().value())
                );
                for (AsmrTypeAnnotationNode annotation : tryCatchBlock.visibleTypeAnnotations()) {
                    AnnotationVisitor av = mv.visitTryCatchAnnotation(
                            annotation.typeRef().value(),
                            TypePath.fromString(annotation.typePath().value()),
                            annotation.desc().value(),
                            true
                    );
                    if (av != null) {
                        annotation.accept(av);
                    }
                }
                for (AsmrTypeAnnotationNode annotation : tryCatchBlock.invisibleTypeAnnotations()) {
                    AnnotationVisitor av = mv.visitTryCatchAnnotation(
                            annotation.typeRef().value(),
                            TypePath.fromString(annotation.typePath().value()),
                            annotation.desc().value(),
                            false
                    );
                    if (av != null) {
                        annotation.accept(av);
                    }
                }
            }

            for (AsmrAbstractInsnNode<?> insn : instructions) {
                insn.accept(mv, localResolver, labelFunction);

                if (insn instanceof AsmrInsnNode) {
                    for (AsmrTypeAnnotationNode annotation : ((AsmrInsnNode<?>) insn).visibleTypeAnnotations()) {
                        AnnotationVisitor av = mv.visitInsnAnnotation(
                                annotation.typeRef().value(),
                                TypePath.fromString(annotation.typePath().value()),
                                annotation.desc().value(),
                                true
                        );
                        if (av != null) {
                            annotation.accept(av);
                        }
                    }
                    for (AsmrTypeAnnotationNode annotation : ((AsmrInsnNode<?>) insn).invisibleTypeAnnotations()) {
                        AnnotationVisitor av = mv.visitInsnAnnotation(
                                annotation.typeRef().value(),
                                TypePath.fromString(annotation.typePath().value()),
                                annotation.desc().value(),
                                false
                        );
                        if (av != null) {
                            annotation.accept(av);
                        }
                    }
                }
            }

            for (AsmrLocalVariableNode localVariable : localVariables) {
                mv.visitLocalVariable(
                        localVariable.name().value(),
                        localVariable.desc().value(),
                        AsmrTreeUtil.toNullableString(localVariable.signature().value()),
                        labelFunction.apply(localVariable.start().value()),
                        labelFunction.apply(localVariable.end().value()),
                        localResolver.applyAsInt(localVariable.index().value())
                );
            }

            acceptLocalVariableAnnotations(mv, labelFunction, localResolver, visibleLocalVariableAnnotations, true);
            acceptLocalVariableAnnotations(mv, labelFunction, localResolver, invisibleLocalVariableAnnotations, false);
        }
    }

    private static void acceptLocalVariableAnnotations(MethodVisitor mv, Function<AsmrIndex, Label> labelFunction, ToIntFunction<AsmrIndex> localResolver, AsmrLocalVariableAnnotationListNode localVariableAnnotations, boolean visible) {
        for (AsmrLocalVariableAnnotationNode annotation : localVariableAnnotations) {
            Label[] start = new Label[annotation.targets().size()];
            Label[] end = new Label[start.length];
            int[] index = new int[start.length];
            for (int i = 0; i < start.length; i++) {
                AsmrLocalVariableAnnotationTargetNode target = annotation.targets().get(i);
                start[i] = labelFunction.apply(target.start().value());
                end[i] = labelFunction.apply(target.end().value());
                index[i] = localResolver.applyAsInt(target.index().value());
            }

            AnnotationVisitor av = mv.visitLocalVariableAnnotation(
                    annotation.typeRef().value(),
                    TypePath.fromString(annotation.typePath().value()),
                    start,
                    end,
                    index,
                    annotation.desc().value(),
                    visible
            );
            if (av != null) {
                annotation.accept(av);
            }
        }
    }
}
