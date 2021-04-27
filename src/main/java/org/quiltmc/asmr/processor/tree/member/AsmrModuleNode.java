package org.quiltmc.asmr.processor.tree.member;

import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.ModuleVisitor;
import org.quiltmc.asmr.processor.tree.AsmrNode;
import org.quiltmc.asmr.processor.tree.AsmrValueListNode;
import org.quiltmc.asmr.processor.tree.AsmrValueNode;

import java.util.Arrays;
import java.util.List;

public class AsmrModuleNode extends AsmrNode<AsmrModuleNode> {
    private final AsmrValueNode<String> name = new AsmrValueNode<>(this);
    private final AsmrValueListNode<Integer> modifiers = new AsmrValueListNode<>(this);
    private final AsmrValueNode<String> version = new AsmrValueNode<>(this);

    // TODO: complete this

    private final List<AsmrNode<?>> children = Arrays.asList(name, modifiers, version);

    public AsmrModuleNode() {
        this(null);
    }

    @ApiStatus.Internal
    public AsmrModuleNode(AsmrNode<?> parent) {
        super(parent);
    }

    @ApiStatus.Internal
    @Override
    public AsmrModuleNode newInstance(AsmrNode<?> parent) {
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

    public void accept(ModuleVisitor mv) {
        mv.visitEnd();
    }
}
