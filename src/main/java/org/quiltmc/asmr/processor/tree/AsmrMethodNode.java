package org.quiltmc.asmr.processor.tree;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.TypePath;

import java.util.Arrays;
import java.util.List;

public class AsmrMethodNode extends AsmrNode<AsmrMethodNode> {
    private final AsmrValueListNode<Integer> modifiers = new AsmrValueListNode<>(this);
    private final AsmrValueNode<String> name = new AsmrValueNode<>(this);
    private final AsmrValueNode<String> desc = new AsmrValueNode<>(this);
    private final AsmrValueNode<String> signature = new AsmrValueNode<>(this);
    private final AsmrValueListNode<String> exceptions = new AsmrValueListNode<>(this);

    private final AsmrParameterListNode parameters = new AsmrParameterListNode(this);
    private final AsmrAnnotationListNode visibleAnnotations = new AsmrAnnotationListNode(this);
    private final AsmrAnnotationListNode invisibleAnnotations = new AsmrAnnotationListNode(this);
    private final AsmrTypeAnnotationListNode visibleTypeAnnotations = new AsmrTypeAnnotationListNode(this);
    private final AsmrTypeAnnotationListNode invisibleTypeAnnotations = new AsmrTypeAnnotationListNode(this);
    private final AsmrValueNode<Integer> visibleAnnotableParameterCount = new AsmrValueNode<>(this);
    private final AsmrAnnotationListListNode visibleParameterAnnotations = new AsmrAnnotationListListNode(this);
    private final AsmrValueNode<Integer> invisibleAnnotableParameterCount = new AsmrValueNode<>(this);
    private final AsmrAnnotationListListNode invisibleParameterAnnotations = new AsmrAnnotationListListNode(this);

    private final AsmrAnnotationValueListNode<?> annotationDefault = new AsmrAnnotationValueListNode<>(this);

    private final AsmrMethodBodyNode body = new AsmrMethodBodyNode(this);

    private final List<AsmrNode<?>> children = Arrays.asList(
            modifiers, name, desc, signature, exceptions,
            parameters, visibleAnnotations, invisibleAnnotations, visibleTypeAnnotations, invisibleTypeAnnotations,
            visibleAnnotableParameterCount, visibleParameterAnnotations, invisibleAnnotableParameterCount, invisibleParameterAnnotations,
            annotationDefault,
            body
    );

    public AsmrMethodNode(AsmrNode<?> parent) {
        super(parent);
    }

    @Override
    AsmrMethodNode newInstance(AsmrNode<?> parent) {
        return new AsmrMethodNode(parent);
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

    public AsmrValueListNode<String> exceptions() {
        return exceptions;
    }

    public AsmrParameterListNode parameters() {
        return parameters;
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

    public AsmrValueNode<Integer> visibleAnnotableParameterCount() {
        return visibleAnnotableParameterCount;
    }

    public AsmrAnnotationListListNode visibleParameterAnnotations() {
        return visibleParameterAnnotations;
    }

    public AsmrValueNode<Integer> invisibleAnnotableParameterCount() {
        return invisibleAnnotableParameterCount;
    }

    public AsmrAnnotationListListNode invisibleParameterAnnotations() {
        return invisibleParameterAnnotations;
    }

    public AsmrAnnotationValueListNode<?> annotationDefault() {
        return annotationDefault;
    }

    public AsmrMethodBodyNode body() {
        return body;
    }

    public void accept(MethodVisitor mv) {
        for (AsmrParameterNode parameter : parameters) {
            mv.visitParameter(
                    AsmrTreeUtil.toNullableString(parameter.name().value()),
                    AsmrTreeUtil.modifierListToFlags(parameter.modifiers())
            );
        }
        if (!annotationDefault.isEmpty()) {
            AnnotationVisitor av = mv.visitAnnotationDefault();
            AsmrAbstractAnnotationNode.acceptArray(av, annotationDefault);
        }
        for (AsmrAnnotationNode annotation : visibleAnnotations) {
            AnnotationVisitor av = mv.visitAnnotation(annotation.desc().value(), true);
            if (av != null) {
                annotation.accept(av);
            }
        }
        for (AsmrAnnotationNode annotation : invisibleAnnotations) {
            AnnotationVisitor av = mv.visitAnnotation(annotation.desc().value(), false);
            if (av != null) {
                annotation.accept(av);
            }
        }
        for (AsmrTypeAnnotationNode annotation : visibleTypeAnnotations) {
            AnnotationVisitor av = mv.visitTypeAnnotation(
                    annotation.typeRef().value(),
                    TypePath.fromString(AsmrTreeUtil.toNullableString(annotation.typePath().value())),
                    annotation.desc().value(),
                    true
            );
            if (av != null) {
                annotation.accept(av);
            }
        }
        for (AsmrTypeAnnotationNode annotation : invisibleTypeAnnotations) {
            AnnotationVisitor av = mv.visitTypeAnnotation(
                    annotation.typeRef().value(),
                    TypePath.fromString(AsmrTreeUtil.toNullableString(annotation.typePath().value())),
                    annotation.desc().value(),
                    false
            );
            if (av != null) {
                annotation.accept(av);
            }
        }
        if (visibleAnnotableParameterCount.value() > 0) {
            mv.visitAnnotableParameterCount(visibleAnnotableParameterCount.value(), true);
        }
        for (int parameter = 0; parameter < visibleParameterAnnotations.size(); parameter++) {
            for (AsmrAnnotationNode annotation : visibleParameterAnnotations.get(parameter)) {
                AnnotationVisitor av = mv.visitParameterAnnotation(parameter, annotation.desc().value(), true);
                if (av != null) {
                    annotation.accept(av);
                }
            }
        }
        if (invisibleAnnotableParameterCount.value() > 0) {
            mv.visitAnnotableParameterCount(invisibleAnnotableParameterCount.value(), false);
        }
        for (int parameter = 0; parameter < invisibleParameterAnnotations.size(); parameter++) {
            for (AsmrAnnotationNode annotation : invisibleParameterAnnotations.get(parameter)) {
                AnnotationVisitor av = mv.visitParameterAnnotation(parameter, annotation.desc().value(), false);
                if (av != null) {
                    annotation.accept(av);
                }
            }
        }
        body.accept(mv);
        mv.visitEnd();
    }
}
