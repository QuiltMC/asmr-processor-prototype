package org.quiltmc.asmr.processor.tree;

import org.objectweb.asm.ClassVisitor;

import java.util.Arrays;
import java.util.List;

public class AsmrClassNode extends AsmrNode<AsmrClassNode> {
    private final AsmrValueNode<Integer> version = new AsmrValueNode<>(this);
    private final AsmrValueListNode<Integer> modifiers = new AsmrValueListNode<>(this);
    private final AsmrValueNode<String> name = new AsmrValueNode<>(this);
    private final AsmrValueNode<String> signature = new AsmrValueNode<>(this);
    private final AsmrValueNode<String> superclass = new AsmrValueNode<>(this);
    private final AsmrValueListNode<String> interfaces = new AsmrValueListNode<>(this);

    private final List<AsmrNode<?>> children = Arrays.asList(version, modifiers, name, signature, superclass, interfaces);

    AsmrClassNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrClassNode newInstance(AsmrNode<?> parent) {
        return new AsmrClassNode(parent);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }

    @Override
    public void copyFrom(AsmrClassNode other) {
        version.copyFrom(other.version);
        modifiers.copyFrom(other.modifiers);
        name.copyFrom(other.name);
        signature.copyFrom(other.signature);
        superclass.copyFrom(other.superclass);
        interfaces.copyFrom(other.interfaces);
    }

    public AsmrValueNode<Integer> version() {
        return version;
    }

    public AsmrValueListNode<Integer> modifiers() {
        return modifiers;
    }

    public AsmrValueNode<String> name() {
        return name;
    }

    public AsmrValueNode<String> signature() {
        return signature;
    }

    public AsmrValueNode<String> superclass() {
        return superclass;
    }

    public AsmrValueListNode<String> interfaces() {
        return interfaces;
    }

    public void accept(ClassVisitor cv) {
        cv.visit(
                version.value(),
                AsmrTreeUtil.modifierListToFlags(modifiers),
                name.value(),
                AsmrTreeUtil.toNullableString(signature.value()),
                AsmrTreeUtil.toNullableString(superclass.value()),
                interfaces.toArray(new String[0])
        );
        cv.visitEnd();
    }
}
