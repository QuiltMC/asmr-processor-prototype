package org.quiltmc.asmr.processor.tree;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

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
                modifierList.add().init(mask);
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

    public static String dump(AsmrNode<?> node) {
        StringBuilder sb = new StringBuilder();
        dump(node, sb, "");
        return sb.toString();
    }

    private static void dump(AsmrNode<?> node, StringBuilder sb, String indent) {
        sb.append(node.getClass().getSimpleName()).append(": ");
        if (node instanceof AsmrValueNode) {
            sb.append(((AsmrValueNode<?>) node).value()).append("\n");
        } else {
            String nestedIndent = indent + "  ";
            sb.append("\n");
            for (AsmrNode<?> child : node.children()) {
                sb.append(nestedIndent).append("[").append(getFieldName(node, child)).append("] ");
                dump(child, sb, nestedIndent);
            }
        }
    }

    // For debugging purposes only!
    private static String getFieldName(AsmrNode<?> parent, AsmrNode<?> child) {
        if (parent instanceof AsmrListNode) {
            return String.valueOf(parent.children().indexOf(child));
        } else {
            for (Class<?> clazz = parent.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    try {
                        if (field.get(parent) == child) {
                            return field.getName();
                        }
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            throw new IllegalArgumentException("Could not find field name for " + child + " in " + parent);
        }
    }
}
