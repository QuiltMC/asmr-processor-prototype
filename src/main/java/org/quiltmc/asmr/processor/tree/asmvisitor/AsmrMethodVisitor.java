package org.quiltmc.asmr.processor.tree.asmvisitor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.TypePath;
import org.quiltmc.asmr.processor.AsmrProcessor;
import org.quiltmc.asmr.processor.tree.AsmrAnnotationNode;
import org.quiltmc.asmr.processor.tree.AsmrMethodNode;
import org.quiltmc.asmr.processor.tree.AsmrTypeAnnotationNode;

public class AsmrMethodVisitor extends MethodVisitor {
    private final AsmrMethodNode methodNode;

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
    public void visitEnd() {
        methodNode.visibleAnnotableParameterCount().init(0);
        methodNode.invisibleAnnotableParameterCount().init(0);
    }
}
