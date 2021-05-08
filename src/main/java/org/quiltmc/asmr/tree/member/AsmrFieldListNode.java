package org.quiltmc.asmr.tree.member;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.asmr.tree.AsmrListNode;
import org.quiltmc.asmr.tree.AsmrNode;

public class AsmrFieldListNode extends AsmrListNode<AsmrFieldNode, AsmrFieldListNode> {
    public AsmrFieldListNode() {
        this(null);
    }

    @ApiStatus.Internal
    public AsmrFieldListNode(AsmrNode<?> parent) {
        super(parent);
    }

    @ApiStatus.Internal
    @Override
    public AsmrFieldListNode newInstance(AsmrNode<?> parent) {
        return new AsmrFieldListNode(parent);
    }

    @Override
    protected AsmrFieldNode newElement() {
        return new AsmrFieldNode(this);
    }

    @Nullable
    public AsmrFieldNode findField(String name) {
        for (AsmrFieldNode field : this) {
            if (field.name().value().equals(name)) {
                return field;
            }
        }
        return null;
    }

    @Nullable
    public AsmrFieldNode findField(String name, String desc) {
        for (AsmrFieldNode field : this) {
            if (field.name().value().equals(name) && field.desc().value().equals(desc)) {
                return field;
            }
        }
        return null;
    }
}
