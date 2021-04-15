package org.quiltmc.asmr.processor.tree.asmvisitor;

import org.objectweb.asm.MethodVisitor;
import org.quiltmc.asmr.processor.AsmrProcessor;
import org.quiltmc.asmr.processor.tree.AsmrMethodNode;

public class AsmrMethodVisitor extends MethodVisitor {
    private final AsmrMethodNode methodNode;

    public AsmrMethodVisitor(AsmrMethodNode methodNode) {
        super(AsmrProcessor.ASM_VERSION);
        this.methodNode = methodNode;
    }

    @Override
    public void visitEnd() {
        methodNode.visibleAnnotableParameterCount().init(0);
        methodNode.invisibleAnnotableParameterCount().init(0);
    }
}
