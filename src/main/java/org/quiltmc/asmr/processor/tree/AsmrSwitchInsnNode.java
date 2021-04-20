package org.quiltmc.asmr.processor.tree;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class AsmrSwitchInsnNode extends AsmrInsnNode<AsmrSwitchInsnNode> {
    private final AsmrValueNode<AsmrIndex> dflt = new AsmrValueNode<>(this);
    private final AsmrSwitchKeyLabelListNode labels = new AsmrSwitchKeyLabelListNode(this);

    private final List<AsmrNode<?>> children = Arrays.asList(visibleTypeAnnotations(), invisibleTypeAnnotations(),
            opcode(), dflt, labels);

    public AsmrSwitchInsnNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrSwitchInsnNode newInstance(AsmrNode<?> parent) {
        return new AsmrSwitchInsnNode(parent);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }

    public AsmrValueNode<AsmrIndex> dflt() {
        return dflt;
    }

    public AsmrSwitchKeyLabelListNode labels() {
        return labels;
    }

    @Override
    void accept(MethodVisitor mv, ToIntFunction<AsmrIndex> localVariableIndexes, Function<AsmrIndex, Label> labelIndexes) {
        // find relevant keys and labels (those not equal to the default label)
        List<AsmrSwitchKeyLabelNode> relevantKeyLabels = new ArrayList<>(labels.size());
        for (AsmrSwitchKeyLabelNode keyLabel : labels) {
            if (keyLabel.label().value() != dflt.value()) {
                relevantKeyLabels.add(keyLabel);
            }
        }

        // sort keys into ascending order
        relevantKeyLabels.sort(Comparator.comparing(keyLabel -> keyLabel.key().value()));
        // find and report duplicate keys
        for (int i = 1; i < relevantKeyLabels.size(); i++) {
            if (relevantKeyLabels.get(i - 1).key().value().equals(relevantKeyLabels.get(i).key().value())) {
                throw new IllegalStateException("Switch instruction contains duplicate keys");
            }
        }
        // find min and max key
        int minKey = relevantKeyLabels.get(relevantKeyLabels.size() - 1).key().value();
        int maxKey = relevantKeyLabels.get(0).key().value();

        // decide which instruction to use
        // see: http://hg.openjdk.java.net/jdk8/jdk8/langtools/file/30db5e0aaf83/src/share/classes/com/sun/tools/javac/jvm/Gen.java#l1153
        long tableSpaceCost = 4 + ((long) maxKey - minKey + 1);
        long tableTimeCost = 3;
        long lookupSpaceCost = 3 + 2 * (long) relevantKeyLabels.size();
        long lookupTimeCost = relevantKeyLabels.size();
        boolean tableswitch = !relevantKeyLabels.isEmpty() && tableSpaceCost + 3 * tableTimeCost <= lookupSpaceCost + 3 * lookupTimeCost;

        Label defaultLabel = labelIndexes.apply(dflt.value());

        if (tableswitch) {
            Label[] labels = new Label[maxKey - minKey + 1];
            Arrays.fill(labels, defaultLabel);
            for (AsmrSwitchKeyLabelNode keyLabel : relevantKeyLabels) {
                labels[keyLabel.key().value() - minKey] = labelIndexes.apply(keyLabel.label().value());
            }
            mv.visitTableSwitchInsn(minKey, maxKey, defaultLabel, labels);
        } else {
            int[] keys = new int[relevantKeyLabels.size()];
            Label[] labels = new Label[relevantKeyLabels.size()];
            for (int i = 0; i < keys.length; i++) {
                AsmrSwitchKeyLabelNode keyLabel = relevantKeyLabels.get(i);
                keys[i] = keyLabel.key().value();
                labels[i] = labelIndexes.apply(keyLabel.label().value());
            }
            mv.visitLookupSwitchInsn(defaultLabel, keys, labels);
        }
    }
}
