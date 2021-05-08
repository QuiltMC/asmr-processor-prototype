package org.quiltmc.asmr.tree.member;

import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.TypePath;
import org.quiltmc.asmr.tree.AsmrNode;
import org.quiltmc.asmr.tree.AsmrTreeUtil;
import org.quiltmc.asmr.tree.AsmrValueListNode;
import org.quiltmc.asmr.tree.AsmrValueNode;
import org.quiltmc.asmr.tree.annotation.AsmrAnnotationListNode;
import org.quiltmc.asmr.tree.annotation.AsmrAnnotationNode;
import org.quiltmc.asmr.tree.annotation.AsmrTypeAnnotationListNode;
import org.quiltmc.asmr.tree.annotation.AsmrTypeAnnotationNode;

import java.util.Arrays;
import java.util.List;

public class AsmrClassNode extends AsmrNode<AsmrClassNode> {
    private final AsmrValueNode<Integer> version = new AsmrValueNode<>(this);
    private final AsmrValueListNode<Integer> modifiers = new AsmrValueListNode<>(this);
    private final AsmrValueNode<String> name = new AsmrValueNode<>(this);
    private final AsmrValueNode<String> signature = new AsmrValueNode<>(this);
    private final AsmrValueNode<String> superclass = new AsmrValueNode<>(this);
    private final AsmrValueListNode<String> interfaces = new AsmrValueListNode<>(this);

    private final AsmrValueNode<String> sourceFile = new AsmrValueNode<>(this);
    private final AsmrValueNode<String> sourceDebug = new AsmrValueNode<>(this);

    private final AsmrModuleNode module = new AsmrModuleNode(this);

    private final AsmrValueNode<String> outerClass = new AsmrValueNode<>(this);
    private final AsmrValueNode<String> outerMethod = new AsmrValueNode<>(this);
    private final AsmrValueNode<String> outerMethodDesc = new AsmrValueNode<>(this);

    private final AsmrAnnotationListNode visibleAnnotations = new AsmrAnnotationListNode(this);
    private final AsmrAnnotationListNode invisibleAnnotations = new AsmrAnnotationListNode(this);

    private final AsmrTypeAnnotationListNode visibleTypeAnnotations = new AsmrTypeAnnotationListNode(this);
    private final AsmrTypeAnnotationListNode invisibleTypeAnnotations = new AsmrTypeAnnotationListNode(this);

    private final AsmrInnerClassListNode innerClasses = new AsmrInnerClassListNode(this);

    private final AsmrValueNode<String> nestHostClass = new AsmrValueNode<>(this);
    private final AsmrValueListNode<String> nestMembers = new AsmrValueListNode<>(this);

    private final AsmrFieldListNode fields = new AsmrFieldListNode(this);
    private final AsmrMethodListNode methods = new AsmrMethodListNode(this);

    private final List<AsmrNode<?>> children = Arrays.asList(
            version, modifiers, name, signature, superclass, interfaces,
            sourceFile, sourceDebug,
            module,
            outerClass, outerMethod, outerMethodDesc,
            visibleAnnotations, invisibleAnnotations,
            visibleTypeAnnotations, invisibleTypeAnnotations,
            innerClasses,
            nestHostClass, nestMembers,
            fields, methods
    );

    public AsmrClassNode() {
        super(null);
    }

    @ApiStatus.Internal
    @Override
    public AsmrClassNode newInstance(AsmrNode<?> parent) {
        return new AsmrClassNode();
    }

    @Override
    public List<AsmrNode<?>> children() {
        return children;
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

    public AsmrValueNode<String> sourceFile() {
        return sourceFile;
    }

    public AsmrValueNode<String> sourceDebug() {
        return sourceDebug;
    }

    public AsmrModuleNode module() {
        return module;
    }

    public AsmrValueNode<String> outerClass() {
        return outerClass;
    }

    public AsmrValueNode<String> outerMethod() {
        return outerMethod;
    }

    public AsmrValueNode<String> outerMethodDesc() {
        return outerMethodDesc;
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

    public AsmrInnerClassListNode innerClasses() {
        return innerClasses;
    }

    public AsmrValueNode<String> nestHostClass() {
        return nestHostClass;
    }

    public AsmrValueListNode<String> nestMembers() {
        return nestMembers;
    }

    public AsmrFieldListNode fields() {
        return fields;
    }

    public AsmrMethodListNode methods() {
        return methods;
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
        cv.visitSource(
                AsmrTreeUtil.toNullableString(sourceFile.value()),
                AsmrTreeUtil.toNullableString(sourceDebug.value())
        );
        if (!module.name().value().isEmpty()) {
            ModuleVisitor mv = cv.visitModule(
                    module.name().value(),
                    AsmrTreeUtil.modifierListToFlags(module.modifiers()),
                    AsmrTreeUtil.toNullableString(module.version().value())
            );
            if (mv != null) {
                module.accept(mv);
            }
        }
        if (!outerClass.value().isEmpty()) {
            cv.visitOuterClass(
                    outerClass.value(),
                    AsmrTreeUtil.toNullableString(outerMethod.value()),
                    AsmrTreeUtil.toNullableString(outerMethodDesc.value())
            );
        }
        for (AsmrAnnotationNode annotation : visibleAnnotations) {
            AnnotationVisitor av = cv.visitAnnotation(annotation.desc().value(), true);
            if (av != null) {
                annotation.accept(av);
            }
        }
        for (AsmrAnnotationNode annotation : invisibleAnnotations) {
            AnnotationVisitor av = cv.visitAnnotation(annotation.desc().value(), false);
            if (av != null) {
                annotation.accept(av);
            }
        }
        for (AsmrTypeAnnotationNode annotation : visibleTypeAnnotations) {
            AnnotationVisitor av = cv.visitTypeAnnotation(
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
            AnnotationVisitor av = cv.visitTypeAnnotation(
                    annotation.typeRef().value(),
                    TypePath.fromString(AsmrTreeUtil.toNullableString(annotation.typePath().value())),
                    annotation.desc().value(),
                    false
            );
            if (av != null) {
                annotation.accept(av);
            }
        }
        for (AsmrInnerClassNode innerClass : innerClasses) {
            cv.visitInnerClass(
                    innerClass.name().value(),
                    AsmrTreeUtil.toNullableString(innerClass.outerName().value()),
                    AsmrTreeUtil.toNullableString(innerClass.innerName().value()),
                    AsmrTreeUtil.modifierListToFlags(innerClass.modifiers())
            );
        }
        if (!nestHostClass.value().isEmpty()) {
            cv.visitNestHost(nestHostClass.value());
        }
        for (AsmrValueNode<String> nestMember : nestMembers) {
            cv.visitNestMember(nestMember.value());
        }
        for (AsmrFieldNode field : fields) {
            FieldVisitor fv = cv.visitField(
                    AsmrTreeUtil.modifierListToFlags(field.modifiers()),
                    field.name().value(),
                    field.desc().value(),
                    AsmrTreeUtil.toNullableString(field.signature().value()),
                    field.value().value() == AsmrFieldNode.NO_VALUE ? null : field.value().value()
            );
            if (fv != null) {
                field.accept(fv);
            }
        }
        for (AsmrMethodNode method : methods) {
            MethodVisitor mv = cv.visitMethod(
                    AsmrTreeUtil.modifierListToFlags(method.modifiers()),
                    method.name().value(),
                    method.desc().value(),
                    AsmrTreeUtil.toNullableString(method.signature().value()),
                    method.exceptions().toArray(new String[0])
            );
            if (mv != null) {
                method.accept(mv);
            }
        }
        cv.visitEnd();
    }
}
