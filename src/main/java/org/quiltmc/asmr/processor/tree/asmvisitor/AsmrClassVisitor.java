package org.quiltmc.asmr.processor.tree.asmvisitor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.TypePath;
import org.quiltmc.asmr.processor.AsmrProcessor;
import org.quiltmc.asmr.processor.tree.AsmrTreeUtil;
import org.quiltmc.asmr.processor.tree.annotation.AsmrAnnotationNode;
import org.quiltmc.asmr.processor.tree.annotation.AsmrTypeAnnotationNode;
import org.quiltmc.asmr.processor.tree.member.AsmrClassNode;
import org.quiltmc.asmr.processor.tree.member.AsmrFieldNode;
import org.quiltmc.asmr.processor.tree.member.AsmrInnerClassNode;
import org.quiltmc.asmr.processor.tree.member.AsmrMethodNode;

public class AsmrClassVisitor extends ClassVisitor {
    public final AsmrClassNode classNode;
    private boolean visitedSource = false;
    private boolean visitedModule = false;
    private boolean visitedOuterClass = false;
    private boolean visitedNestHost = false;

    public AsmrClassVisitor(AsmrClassNode classNode) {
        super(AsmrProcessor.ASM_VERSION);
        this.classNode = classNode;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        classNode.version().init(version);
        AsmrTreeUtil.flagsToModifierList(access, classNode.modifiers());
        classNode.name().init(name);
        classNode.signature().init(AsmrTreeUtil.fromNullableString(signature));
        classNode.superclass().init(AsmrTreeUtil.fromNullableString(superName));
        if (interfaces != null) {
            for (String itf : interfaces) {
                classNode.interfaces().add().init(itf);
            }
        }
    }

    @Override
    public void visitSource(String source, String debug) {
        visitedSource = true;
        classNode.sourceFile().init(AsmrTreeUtil.fromNullableString(source));
        classNode.sourceDebug().init(AsmrTreeUtil.fromNullableString(debug));
    }

    @Override
    public ModuleVisitor visitModule(String name, int access, String version) {
        visitedModule = true;
        classNode.module().name().init(name);
        AsmrTreeUtil.flagsToModifierList(access, classNode.module().modifiers());
        classNode.module().version().init(AsmrTreeUtil.fromNullableString(version));
        return null; // TODO: AsmrModuleVisitor
    }

    @Override
    public void visitOuterClass(String owner, String name, String descriptor) {
        visitedOuterClass = true;
        classNode.outerClass().init(owner);
        classNode.outerMethod().init(AsmrTreeUtil.fromNullableString(name));
        classNode.outerMethodDesc().init(AsmrTreeUtil.fromNullableString(descriptor));
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        AsmrAnnotationNode annotation = visible ? classNode.visibleAnnotations().add() : classNode.invisibleAnnotations().add();
        annotation.desc().init(descriptor);
        return new AsmrAnnotationVisitor(annotation);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        AsmrTypeAnnotationNode annotation = visible ? classNode.visibleTypeAnnotations().add() : classNode.invisibleTypeAnnotations().add();
        annotation.typeRef().init(typeRef);
        annotation.typePath().init(typePath == null ? "" : typePath.toString());
        annotation.desc().init(descriptor);
        return new AsmrAnnotationVisitor(annotation);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        AsmrInnerClassNode innerClass = classNode.innerClasses().add();
        innerClass.name().init(name);
        innerClass.outerName().init(AsmrTreeUtil.fromNullableString(outerName));
        innerClass.innerName().init(AsmrTreeUtil.fromNullableString(innerName));
        AsmrTreeUtil.flagsToModifierList(access, innerClass.modifiers());
    }

    @Override
    public void visitNestHost(String nestHost) {
        visitedNestHost = true;
        classNode.nestHostClass().init(nestHost);
    }

    @Override
    public void visitNestMember(String nestMember) {
        classNode.nestMembers().add().init(nestMember);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        AsmrFieldNode field = classNode.fields().add();
        AsmrTreeUtil.flagsToModifierList(access, field.modifiers());
        field.name().init(name);
        field.desc().init(descriptor);
        field.signature().init(signature);
        field.value().init(value == null ? AsmrFieldNode.NO_VALUE : value);
        return new AsmrFieldVisitor(field);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        AsmrMethodNode method = classNode.methods().add();
        AsmrTreeUtil.flagsToModifierList(access, method.modifiers());
        method.name().init(name);
        method.desc().init(descriptor);
        method.signature().init(AsmrTreeUtil.fromNullableString(signature));
        if (exceptions != null) {
            for (String exception : exceptions) {
                method.exceptions().add().init(exception);
            }
        }
        return new AsmrMethodVisitor(method);
    }

    @Override
    public void visitEnd() {
        if (!visitedSource) {
            classNode.sourceFile().init("");
            classNode.sourceDebug().init("");
        }
        if (!visitedModule) {
            classNode.module().name().init("");
            classNode.module().version().init("");
            // TODO: AsmrModuleVisitor
        }
        if (!visitedOuterClass) {
            classNode.outerClass().init("");
            classNode.outerMethod().init("");
            classNode.outerMethodDesc().init("");
        }
        if (!visitedNestHost) {
            classNode.nestHostClass().init("");
        }
    }
}
