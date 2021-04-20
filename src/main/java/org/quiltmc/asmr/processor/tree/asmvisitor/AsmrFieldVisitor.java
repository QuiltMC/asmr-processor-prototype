package org.quiltmc.asmr.processor.tree.asmvisitor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.TypePath;
import org.quiltmc.asmr.processor.AsmrProcessor;
import org.quiltmc.asmr.processor.tree.annotation.AsmrAnnotationNode;
import org.quiltmc.asmr.processor.tree.annotation.AsmrTypeAnnotationNode;
import org.quiltmc.asmr.processor.tree.member.AsmrFieldNode;

public class AsmrFieldVisitor extends FieldVisitor {
    private final AsmrFieldNode fieldNode;

    public AsmrFieldVisitor(AsmrFieldNode fieldNode) {
        super(AsmrProcessor.ASM_VERSION);
        this.fieldNode = fieldNode;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        AsmrAnnotationNode annotation = visible ? fieldNode.visibleAnnotations().add() : fieldNode.invisibleAnnotations().add();
        annotation.desc().init(descriptor);
        return new AsmrAnnotationVisitor(annotation);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        AsmrTypeAnnotationNode annotation = visible ? fieldNode.visibleTypeAnnotations().add() : fieldNode.invisibleTypeAnnotations().add();
        annotation.typeRef().init(typeRef);
        annotation.typePath().init(typePath == null ? "" : typePath.toString());
        annotation.desc().init(descriptor);
        return new AsmrAnnotationVisitor(annotation);
    }
}
