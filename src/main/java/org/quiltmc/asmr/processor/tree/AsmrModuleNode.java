package org.quiltmc.asmr.processor.tree;

import org.objectweb.asm.ModuleVisitor;

import java.util.Arrays;
import java.util.List;

public class AsmrModuleNode extends AsmrNode<AsmrModuleNode> {
    private final AsmrValueNode<String> name = new AsmrValueNode<>(this);
    private final AsmrValueListNode<Integer> modifiers = new AsmrValueListNode<>(this);
    private final AsmrValueNode<String> version = new AsmrValueNode<>(this);

    // TODO: complete this

    private final List<AsmrNode<?>> children = Arrays.asList(name, modifiers, version);

    public AsmrModuleNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrModuleNode newInstance(AsmrNode<?> parent) {
        return new AsmrModuleNode(parent);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }

    public AsmrValueNode<String> name() {
        return name;
    }

    public AsmrValueListNode<Integer> modifiers() {
        return modifiers;
    }

    public AsmrValueNode<String> version() {
        return version;
    }

    @Override
    void copyFrom(AsmrModuleNode other) {
        name.copyFrom(other.name);
        modifiers.copyFrom(other.modifiers);
        version.copyFrom(other.version);
    }

    public void accept(ModuleVisitor mv) {
        mv.visitEnd();
    }
}
