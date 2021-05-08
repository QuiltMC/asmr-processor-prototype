package org.quiltmc.asmr.processor;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.processor.annotation.HideFromTransformers;
import org.quiltmc.asmr.util.Pair;
import org.quiltmc.asmr.util.Triple;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

@ApiStatus.Experimental // we don't know whether predicating on the constant pool is worth it yet
public class AsmrConstantPool {
    private static final int MAX_JAVA_VERSION = 16;
    private static final int[] TAG_SKIPS = {-1, -1, -1, 4, 4, 8, 8, 2, 2, 4, 4, 4, 4, -1, -1, 3, 2, 4, 4, 2, 2};

    private final Set<String> utfs = new HashSet<>();
    private final Set<Integer> integers = new HashSet<>();
    private final Set<Float> floats = new HashSet<>();
    private final Set<Long> longs = new HashSet<>();
    private final Set<Double> doubles = new HashSet<>();
    private final Set<String> classes = new HashSet<>();
    private final Set<String> strings = new HashSet<>();
    private final Set<Triple<String, String, String>> fieldRefs = new HashSet<>();
    private final Set<Triple<String, String, String>> methodRefs = new HashSet<>();
    private final Set<Triple<String, String, String>> interfaceMethodRefs = new HashSet<>();
    private final Set<Pair<String, String>> nameAndTypes = new HashSet<>();
    private final Set<Handle> methodHandles = new HashSet<>();
    private final Set<String> methodTypes = new HashSet<>();
    private final Set<String> modules = new HashSet<>();
    private final Set<String> packages = new HashSet<>();

    private AsmrConstantPool() {}

    // ===== PREDICATE METHODS ===== //

    public boolean hasUtf(String value) {
        return utfs.contains(value);
    }

    public boolean hasInteger(int value) {
        return integers.contains(value);
    }

    public boolean hasFloat(float value) {
        return floats.contains(value);
    }

    public boolean hasLong(long value) {
        return longs.contains(value);
    }

    public boolean hasDouble(double value) {
        return doubles.contains(value);
    }

    public boolean hasClass(String value) {
        return classes.contains(value);
    }

    public boolean hasString(String value) {
        return strings.contains(value);
    }

    public boolean hasFieldRef(String owner, String name, String desc) {
        return fieldRefs.contains(Triple.of(owner, name, desc));
    }

    public boolean hasMethodRef(String owner, String name, String desc) {
        return methodRefs.contains(Triple.of(owner, name, desc));
    }

    public boolean hasInterfaceMethodRef(String owner, String name, String desc) {
        return interfaceMethodRefs.contains(Triple.of(owner, name, desc));
    }

    public boolean hasNameAndType(String name, String type) {
        return nameAndTypes.contains(Pair.of(name, type));
    }

    public boolean hasMethodHandle(int kind, String owner, String name, String desc, boolean itf) {
        return methodHandles.contains(new Handle(kind, owner, name, desc, itf));
    }

    public boolean hasMethodType(String value) {
        return methodTypes.contains(value);
    }

    public boolean hasModule(String value) {
        return modules.contains(value);
    }

    public boolean hasPackage(String value) {
        return packages.contains(value);
    }

    // ===== READING ===== //

    /** Warning: will be slow if the input stream is not buffered! */
    @HideFromTransformers
    public static AsmrConstantPool read(InputStream in) throws IOException {
        byte[] headerBuffer = new byte[10];
        readBytes(in, headerBuffer, 0);
        int majorVersion = readUnsignedShort(headerBuffer, 6);
        if (majorVersion > 44 + MAX_JAVA_VERSION) {
            throw new IllegalArgumentException("Class file version " + majorVersion + " not supported");
        }
        int constantPoolCount = readUnsignedShort(headerBuffer, 8);
        byte[][] entries = new byte[constantPoolCount][];
        entries[0] = headerBuffer;
        int totalLength = 10;

        for (int constantPoolIndex = 1; constantPoolIndex < constantPoolCount; constantPoolIndex++) {
            int tag = in.read();
            if (tag == 1) { // CONSTANT_Utf8
                int a = in.read();
                int b = in.read();
                int byteLength = (a << 8) | b;
                byte[] entry = entries[constantPoolIndex] = new byte[byteLength + 3];
                entry[0] = (byte) tag;
                entry[1] = (byte) a;
                entry[2] = (byte) b;
                readBytes(in, entry, 3);
                totalLength += byteLength + 3;
            } else {
                if (tag < 0 || tag >= TAG_SKIPS.length) {
                    throw new IllegalArgumentException("Unknown constant tag " + tag);
                }
                int skip = TAG_SKIPS[tag];
                if (skip == -1) {
                    throw new IllegalArgumentException("Unknown constant tag " + tag);
                }
                byte[] entry = entries[constantPoolIndex] = new byte[skip + 1];
                entry[0] = (byte) tag;
                readBytes(in, entry, 1);
                totalLength += skip + 1;
                if (tag == 5 || tag == 6) { // CONSTANT_Long || CONSTANT_Double
                    constantPoolIndex++;
                }
            }
        }

        byte[] bytecode = new byte[totalLength];
        int index = 0;
        for (byte[] entry : entries) {
            if (entry != null) {
                System.arraycopy(entry, 0, bytecode, index, entry.length);
                index += entry.length;
            }
        }

        return read(bytecode);
    }

    private static void readBytes(InputStream in, byte[] out, int index) throws IOException {
        if (index == out.length) {
            // Can happen e.g. with the empty string
            return;
        }
        int amtRead = -1;
        while (index < out.length && (amtRead = in.read(out, index, out.length - index)) != -1) {
            index += amtRead;
        }
        if (amtRead == -1) {
            throw new EOFException();
        }
    }

    @HideFromTransformers
    public static AsmrConstantPool read(byte[] bytecode) {
        if (readInt(bytecode, 0) != 0xcafebabe) {
            throw new IllegalArgumentException("Invalid class file magic");
        }
        int majorVersion = readUnsignedShort(bytecode, 6);
        if (majorVersion > 44 + MAX_JAVA_VERSION) {
            throw new IllegalArgumentException("Class file version " + majorVersion + " not supported");
        }

        int constantPoolCount = readUnsignedShort(bytecode, 8);

        String[] utfs = new String[constantPoolCount];
        short[] firstProperties = new short[constantPoolCount];
        short[] secondProperties = new short[constantPoolCount];
        BitSet isInterfaceMethodref = new BitSet(constantPoolCount);

        // First pass through the constant pool, collect the stuff that other entries have references to
        int index = 10;
        for (int constantPoolIndex = 1; constantPoolIndex < constantPoolCount; constantPoolIndex++) {
            int tag = readUnsignedByte(bytecode, index++);
            if (tag == 1) { // CONSTANT_Utf8
                StringBuilder string = new StringBuilder();
                index = readUtf(bytecode, index, string);
                utfs[constantPoolIndex] = string.toString();
            } else {
                if (tag == 7) { // CONSTANT_Class
                    firstProperties[constantPoolIndex] = (short) readUnsignedShort(bytecode, index);
                } else if (tag >= 9 && tag <= 12) { // CONSTANT_Fieldref || CONSTANT_Methodref || CONSTANT_InterfaceMethodref || CONSTANT_NameAndType
                    firstProperties[constantPoolIndex] = (short) readUnsignedShort(bytecode, index);
                    secondProperties[constantPoolIndex] = (short) readUnsignedShort(bytecode, index + 2);
                    if (tag == 11) { // CONSTANT_InterfaceMethodref
                        isInterfaceMethodref.set(constantPoolIndex);
                    }
                }

                if (tag > TAG_SKIPS.length) {
                    throw new IllegalArgumentException("Unknown constant tag " + tag);
                }
                int skip = TAG_SKIPS[tag];
                if (skip == -1) {
                    throw new IllegalArgumentException("Unknown constant tag " + tag);
                }
                index += skip;
                if (tag == 5 || tag == 6) { // CONSTANT_Long || CONSTANT_Double
                    constantPoolIndex++;
                }
            }
        }

        // Second pass through the constant pool, resolve references and collect entries into the constant pool object
        AsmrConstantPool constantPool = new AsmrConstantPool();

        index = 10;
        for (int constantPoolIndex = 1; constantPoolIndex < constantPoolCount; constantPoolIndex++) {
            int tag = readUnsignedByte(bytecode, index++);
            switch (tag) {
                case 1: { // CONSTANT_Utf8
                    constantPool.utfs.add(utfs[constantPoolIndex]);
                }
                break;
                case 3: { // CONSTANT_Integer
                    constantPool.integers.add(readInt(bytecode, index));
                }
                break;
                case 4: { // CONSTANT_Float
                    constantPool.floats.add(Float.intBitsToFloat(readInt(bytecode, index)));
                }
                break;
                case 5: { // CONSTANT_Long
                    constantPool.longs.add(readLong(bytecode, index));
                }
                break;
                case 6: { // CONSTANT_Double
                    constantPool.doubles.add(Double.longBitsToDouble(readLong(bytecode, index)));
                }
                break;
                case 7: { // CONSTANT_Class
                    constantPool.classes.add(utfs[readUnsignedShort(bytecode, index)]);
                }
                break;
                case 8: { // CONSTANT_String
                    constantPool.strings.add(utfs[readUnsignedShort(bytecode, index)]);
                }
                break;
                case 9: { // CONSTANT_Fieldref
                    int nameAndTypeIndex = readUnsignedShort(bytecode, index + 2);
                    constantPool.fieldRefs.add(Triple.of(
                            utfs[firstProperties[readUnsignedShort(bytecode, index)] & 0xffff],
                            utfs[firstProperties[nameAndTypeIndex] & 0xffff],
                            utfs[secondProperties[nameAndTypeIndex] & 0xffff])
                    );
                }
                break;
                case 10: { // CONSTANT_Methodref
                    int nameAndTypeIndex = readUnsignedShort(bytecode, index + 2);
                    constantPool.methodRefs.add(Triple.of(
                            utfs[firstProperties[readUnsignedShort(bytecode, index)] & 0xffff],
                            utfs[firstProperties[nameAndTypeIndex] & 0xffff],
                            utfs[secondProperties[nameAndTypeIndex] & 0xffff])
                    );
                }
                break;
                case 11: { // CONSTANT_InterfaceMethodref
                    int nameAndTypeIndex = readUnsignedShort(bytecode, index + 2);
                    constantPool.interfaceMethodRefs.add(Triple.of(
                            utfs[firstProperties[readUnsignedShort(bytecode, index)] & 0xffff],
                            utfs[firstProperties[nameAndTypeIndex] & 0xffff],
                            utfs[secondProperties[nameAndTypeIndex] & 0xffff])
                    );
                }
                break;
                case 12: { // CONSTANT_NameAndType
                    constantPool.nameAndTypes.add(Pair.of(
                            utfs[readUnsignedShort(bytecode, index)],
                            utfs[readUnsignedShort(bytecode, index + 2)]
                    ));
                }
                break;
                case 15: { // CONSTANT_MethodHandle
                    int refIndex = readUnsignedShort(bytecode, index + 1);
                    int nameAndTypeIndex = secondProperties[refIndex] & 0xffff;
                    constantPool.methodHandles.add(new Handle(
                            readUnsignedByte(bytecode, index),
                            utfs[firstProperties[firstProperties[refIndex] & 0xffff] & 0xffff],
                            utfs[firstProperties[nameAndTypeIndex] & 0xffff],
                            utfs[secondProperties[nameAndTypeIndex] & 0xffff],
                            isInterfaceMethodref.get(refIndex)
                    ));
                }
                break;
                case 16: { // CONSTANT_MethodType
                    constantPool.methodTypes.add(utfs[readUnsignedShort(bytecode, index)]);
                }
                break;
                case 19: { // CONSTANT_Module
                    constantPool.modules.add(utfs[readUnsignedShort(bytecode, index)]);
                }
                break;
                case 20: { // CONSTANT_Package
                    constantPool.packages.add(utfs[readUnsignedShort(bytecode, index)]);
                }
                break;
            }

            if (tag == 1) { // CONSTANT_Utf8
                index += readUnsignedShort(bytecode, index) + 2; // byte length
            } else {
                if (tag > TAG_SKIPS.length) {
                    throw new IllegalArgumentException("Unknown constant tag " + tag);
                }
                int skip = TAG_SKIPS[tag];
                if (skip == -1) {
                    throw new IllegalArgumentException("Unknown constant tag " + tag);
                }
                index += skip;
                if (tag == 5 || tag == 6) { // CONSTANT_Long || CONSTANT_Double
                    constantPoolIndex++;
                }
            }

        }

        return constantPool;
    }

    private static int readUnsignedByte(byte[] bytecode, int index) {
        return bytecode[index] & 0xff;
    }

    private static int readUnsignedShort(byte[] bytecode, int index) {
        return (readUnsignedByte(bytecode, index) << 8) | readUnsignedByte(bytecode, index + 1);
    }

    private static int readInt(byte[] bytecode, int index) {
        return (readUnsignedShort(bytecode, index) << 16) | readUnsignedShort(bytecode, index + 2);
    }

    private static long readLong(byte[] bytecode, int index) {
        return ((long) readInt(bytecode, index) << 32) | Integer.toUnsignedLong(readInt(bytecode, index + 4));
    }

    private static int readUtf(byte[] bytecode, int index, StringBuilder output) {
        int byteLength = readUnsignedShort(bytecode, index);
        output.ensureCapacity(byteLength);

        int i = 0;
        while (i < byteLength) {
            int x = readUnsignedByte(bytecode, index + 2 + i++);
            if ((x & 0b1000_0000) == 0) {
                // one byte
                output.append((char) x);
            } else {
                int y = readUnsignedByte(bytecode, index + 2 + i++);
                if ((x & 0b1110_0000) == 0b1100_0000) {
                    // two bytes
                    output.append((char) (((x & 0x1f) << 6) + (y & 0x3f)));
                } else if ((x & 0b1111_0000) == 0b1110_0000) {
                    // three bytes
                    int z = readUnsignedByte(bytecode, index + 2 + i++);
                    output.append((char) (((x & 0xf) << 12) + ((y & 0x3f) << 6) + (z & 0x3f)));
                } else {
                    throw new IllegalArgumentException("Invalid modified UTF-8 codepoint");
                }
            }
        }

        return index + 2 + i;
    }

    // ===== UTILITIES ===== //

    private static class Handle {
        public final int kind;
        public final String owner;
        public final String name;
        public final String desc;
        public final boolean itf;

        public Handle(int kind, String owner, String name, String desc, boolean itf) {
            this.kind = kind;
            this.owner = owner;
            this.name = name;
            this.desc = desc;
            this.itf = itf;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Handle that = (Handle) o;

            if (kind != that.kind) return false;
            if (itf != that.itf) return false;
            if (!owner.equals(that.owner)) return false;
            if (!name.equals(that.name)) return false;
            return desc.equals(that.desc);
        }

        @Override
        public int hashCode() {
            int result = kind;
            result = 31 * result + owner.hashCode();
            result = 31 * result + name.hashCode();
            result = 31 * result + desc.hashCode();
            result = 31 * result + (itf ? 1 : 0);
            return result;
        }
    }
}
