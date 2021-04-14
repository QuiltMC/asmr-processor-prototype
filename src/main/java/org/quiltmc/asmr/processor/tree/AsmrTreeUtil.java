package org.quiltmc.asmr.processor.tree;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AsmrTreeUtil {
    private AsmrTreeUtil() {}

    public static int modifierListToFlags(AsmrValueListNode<Integer> modifierList) {
        int flags = 0;
        for (AsmrValueNode<Integer> mod : modifierList) {
            flags |= mod.value();
        }
        return flags;
    }

    public static void flagsToModifierList(int flags, AsmrValueListNode<Integer> modifierList) {
        int mask = 1;
        while (flags != 0) {
            if ((flags & mask) != 0) {
                modifierList.newElement().init(mask);
                flags &= ~mask;
            }
            mask <<= 1;
        }
    }

    @Nullable
    public static String toNullableString(@NotNull String from) {
        return from.isEmpty() ? null : from;
    }

    @NotNull
    public static String fromNullableString(@Nullable String from) {
        return from == null ? "" : from;
    }
}
