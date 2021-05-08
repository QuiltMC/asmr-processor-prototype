package org.quiltmc.asmr.tree.member;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.asmr.tree.AsmrListNode;
import org.quiltmc.asmr.tree.AsmrNode;

public class AsmrMethodListNode extends AsmrListNode<AsmrMethodNode, AsmrMethodListNode> {
    public AsmrMethodListNode() {
        this(null);
    }

    @ApiStatus.Internal
    public AsmrMethodListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @ApiStatus.Internal
    @Override
    public AsmrMethodListNode newInstance(AsmrNode<?> parent) {
        return new AsmrMethodListNode(parent);
    }

    @Override
    protected AsmrMethodNode newElement() {
        return new AsmrMethodNode(this);
    }

    @Nullable
    public AsmrMethodNode findMethod(String name, String desc) {
        for (AsmrMethodNode method : this) {
            if (method.name().value().equals(name) && method.desc().value().equals(desc)) {
                return method;
            }
        }
        return null;
    }
}
