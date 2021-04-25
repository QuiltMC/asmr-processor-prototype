package org.quiltmc.asmr.processor.tree.member;

import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.TypePath;
import org.quiltmc.asmr.processor.tree.AsmrNode;
import org.quiltmc.asmr.processor.tree.AsmrValueListNode;
import org.quiltmc.asmr.processor.tree.AsmrValueNode;
import org.quiltmc.asmr.processor.tree.annotation.AsmrAnnotationListNode;
import org.quiltmc.asmr.processor.tree.annotation.AsmrAnnotationNode;
import org.quiltmc.asmr.processor.tree.annotation.AsmrTypeAnnotationListNode;
import org.quiltmc.asmr.processor.tree.annotation.AsmrTypeAnnotationNode;

import java.util.Arrays;
import java.util.List;

public class AsmrFieldNode extends AsmrNode<AsmrFieldNode> {
    public static final Object NO_VALUE = new Object();

    private final AsmrValueListNode<Integer> modifiers = new AsmrValueListNode<>(this);
    private final AsmrValueNode<String> name = new AsmrValueNode<>(this);
    private final AsmrValueNode<String> desc = new AsmrValueNode<>(this);
    private final AsmrValueNode<String> signature = new AsmrValueNode<>(this);
    private final AsmrValueNode<Object> value = new AsmrValueNode<>(this);

    private final AsmrAnnotationListNode visibleAnnotations = new AsmrAnnotationListNode(this);
    private final AsmrAnnotationListNode invisibleAnnotations = new AsmrAnnotationListNode(this);
    private final AsmrTypeAnnotationListNode visibleTypeAnnotations = new AsmrTypeAnnotationListNode(this);
    private final AsmrTypeAnnotationListNode invisibleTypeAnnotations = new AsmrTypeAnnotationListNode(this);

    private final List<AsmrNode<?>> children = Arrays.asList(
            modifiers, name, desc, signature, value,
            visibleAnnotations, invisibleAnnotations, visibleTypeAnnotations, invisibleTypeAnnotations
    );

    public AsmrFieldNode(AsmrNode<?> parent) {
        super(parent);
    }

    @ApiStatus.Internal
    @Override
    public AsmrFieldNode newInstance(AsmrNode<?> parent) {
        return new AsmrFieldNode(parent);
    }

    @Override
    public List<AsmrNode<?>> children() {
        return children;
    }

    public AsmrValueListNode<Integer> modifiers() {
        return modifiers;
    }

    public AsmrValueNode<String> name() {
        return name;
    }

    public AsmrValueNode<String> desc() {
        return desc;
    }

    public AsmrValueNode<String> signature() {
        return signature;
    }

    public AsmrValueNode<Object> value() {
        return value;
    }

    public AsmrAnnotationListNode visibleAnnotations() {
        return visibleAnnotations;
    }

    public AsmrAnnotationListNode invisibleAnnotations() {
        return invisibleAnnotations;
    }

    public AsmrTypeAnnotationListNode visibleTypeAnnotations() {
        return visibleTypeAnnotations;
    }

    public AsmrTypeAnnotationListNode invisibleTypeAnnotations() {
        return invisibleTypeAnnotations;
    }

    public void accept(FieldVisitor fv) {
        for (AsmrAnnotationNode annotation : visibleAnnotations) {
            AnnotationVisitor av = fv.visitAnnotation(annotation.desc().value(), true);
            if (av != null) {
                annotation.accept(av);
            }
        }
        for (AsmrAnnotationNode annotation : invisibleAnnotations) {
            AnnotationVisitor av = fv.visitAnnotation(annotation.desc().value(), false);
            if (av != null) {
                annotation.accept(av);
            }
        }
        for (AsmrTypeAnnotationNode annotation : visibleTypeAnnotations) {
            AnnotationVisitor av = fv.visitTypeAnnotation(
                    annotation.typeRef().value(),
                    TypePath.fromString(annotation.typePath().value()),
                    annotation.desc().value(),
                    true
            );
            if (av != null) {
                annotation.accept(av);
            }
        }
        for (AsmrTypeAnnotationNode annotation : invisibleTypeAnnotations) {
            AnnotationVisitor av = fv.visitTypeAnnotation(
                    annotation.typeRef().value(),
                    TypePath.fromString(annotation.typePath().value()),
                    annotation.desc().value(),
                    false
            );
            if (av != null) {
                annotation.accept(av);
            }
        }
        fv.visitEnd();
    }
}
