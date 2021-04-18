package org.quiltmc.asmr.processor.tree.asmvisitor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.quiltmc.asmr.processor.AsmrProcessor;
import org.quiltmc.asmr.processor.tree.AsmrAnnotationListListNode;
import org.quiltmc.asmr.processor.tree.AsmrAnnotationNode;
import org.quiltmc.asmr.processor.tree.AsmrMethodNode;
import org.quiltmc.asmr.processor.tree.AsmrParameterNode;
import org.quiltmc.asmr.processor.tree.AsmrTreeUtil;
import org.quiltmc.asmr.processor.tree.AsmrTypeAnnotationNode;

public class AsmrMethodVisitor extends MethodVisitor {
    private final AsmrMethodNode methodNode;
    private boolean visitedVisibleAnnotableParameterCount = false;
    private boolean visitedInvisibleAnnotableParameterCount = false;

    public AsmrMethodVisitor(AsmrMethodNode methodNode) {
        super(AsmrProcessor.ASM_VERSION);
        this.methodNode = methodNode;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        AsmrAnnotationNode annotation = visible ? methodNode.visibleAnnotations().add() : methodNode.invisibleAnnotations().add();
        annotation.desc().init(descriptor);
        return new AsmrAnnotationVisitor(annotation);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        AsmrTypeAnnotationNode annotation = visible ? methodNode.visibleTypeAnnotations().add() : methodNode.invisibleTypeAnnotations().add();
        annotation.typeRef().init(typeRef);
        annotation.typePath().init(typePath == null ? "" : typePath.toString());
        annotation.desc().init(descriptor);
        return new AsmrAnnotationVisitor(annotation);
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        return new AsmrAnnotationVisitor.ArrayVisitor<>(methodNode.annotationDefault());
    }

    @Override
    public void visitParameter(String name, int access) {
        AsmrParameterNode parameter = methodNode.parameters().add();
        parameter.name().init(AsmrTreeUtil.fromNullableString(name));
        AsmrTreeUtil.flagsToModifierList(access, parameter.modifiers());
    }

    @Override
    public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
        if (visible) {
            visitedVisibleAnnotableParameterCount = true;
            methodNode.visibleAnnotableParameterCount().init(parameterCount);
        } else {
            visitedInvisibleAnnotableParameterCount = true;
            methodNode.invisibleAnnotableParameterCount().init(parameterCount);
        }
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        AsmrAnnotationListListNode parameterAnnotations = visible ? methodNode.visibleParameterAnnotations() : methodNode.invisibleParameterAnnotations();
        if (parameterAnnotations.isEmpty()) {
            for (int i = 0, e = Type.getArgumentTypes(methodNode.desc().value()).length; i < e; i++) {
                parameterAnnotations.add();
            }
        }
        AsmrAnnotationNode annotation = parameterAnnotations.get(parameter).add();
        annotation.desc().init(descriptor);
        return new AsmrAnnotationVisitor(annotation);
    }

    @Override
    public void visitEnd() {
        if (!visitedVisibleAnnotableParameterCount) {
            methodNode.visibleAnnotableParameterCount().init(0);
        }
        if (!visitedInvisibleAnnotableParameterCount) {
            methodNode.invisibleAnnotableParameterCount().init(0);
        }
    }
}
