package org.quiltmc.asmr.processor.tree.insn;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.quiltmc.asmr.processor.tree.AsmrNode;
import org.quiltmc.asmr.processor.tree.AsmrValueNode;

import java.util.Arrays;
import java.util.List;

public class AsmrHandleNode extends AsmrNode<AsmrHandleNode> {
    private final AsmrValueNode<Tag> tag = new AsmrValueNode<>(this);
    private final AsmrValueNode<String> owner = new AsmrValueNode<>(this);
    private final AsmrValueNode<String> name = new AsmrValueNode<>(this);
    private final AsmrValueNode<String> desc = new AsmrValueNode<>(this);
    private final AsmrValueNode<Boolean> itf = new AsmrValueNode<>(this);

    private final List<AsmrNode<?>> children = Arrays.asList(tag, owner, name, desc, itf);

    public AsmrHandleNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    protected AsmrHandleNode newInstance(AsmrNode<?> parent) {
        return new AsmrHandleNode(parent);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }

    public AsmrValueNode<Tag> tag() {
        return tag;
    }

    public AsmrValueNode<String> owner() {
        return owner;
    }

    public AsmrValueNode<String> name() {
        return name;
    }

    public AsmrValueNode<String> desc() {
        return desc;
    }

    public AsmrValueNode<Boolean> itf() {
        return itf;
    }

    public enum Tag {
        GETFIELD(Opcodes.H_GETFIELD),
        GETSTATIC(Opcodes.H_GETSTATIC),
        PUTFIELD(Opcodes.H_PUTFIELD),
        PUTSTATIC(Opcodes.H_PUTSTATIC),
        INVOKEVIRTUAL(Opcodes.H_INVOKEVIRTUAL),
        INVOKESTATIC(Opcodes.H_INVOKESTATIC),
        INVOKESPECIAL(Opcodes.H_INVOKESPECIAL),
        NEWINVOKESPECIAL(Opcodes.H_NEWINVOKESPECIAL),
        INVOKEINTERFACE(Opcodes.H_INVOKEINTERFACE),
        ;

        private final int value;
        private static final Tag[] BY_VALUE;

        Tag(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        @Nullable
        public static Tag byValue(int value) {
            return value < 0 || value >= BY_VALUE.length ? null : BY_VALUE[value];
        }

        static {
            int maxValue = 0;
            for (Tag tag : values()) {
                if (tag.value > maxValue) {
                    maxValue = tag.value;
                }
            }
            BY_VALUE = new Tag[maxValue + 1];
            for (Tag tag : values()) {
                BY_VALUE[tag.value] = tag;
            }
        }
    }
}
