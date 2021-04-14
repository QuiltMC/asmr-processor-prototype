package org.quiltmc.asmr.processor.tree.asmvisitor;

import org.objectweb.asm.ClassVisitor;
import org.quiltmc.asmr.processor.AsmrProcessor;
import org.quiltmc.asmr.processor.tree.AsmrClassNode;
import org.quiltmc.asmr.processor.tree.AsmrTreeUtil;

public class AsmrClassVisitor extends ClassVisitor {
    public final AsmrClassNode classNode;

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
}
