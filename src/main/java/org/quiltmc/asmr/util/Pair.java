package org.quiltmc.asmr.util;

public class Pair<K, V> {
    public final K k;
    public final V v;

    public static <K, V> Pair<K, V> of(K k, V v) {
        return new Pair<>(k, v);
    }

    private Pair(K k, V v) {
        this.k = k;
        this.v = v;
    }

    @Override
    public int hashCode() {
        return k.hashCode() + 31 * v.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Pair)) return false;
        Pair<?, ?> that = (Pair<?, ?>) obj;
        return k.equals(that.k) && v.equals(that.v);
    }

    @Override
    public String toString() {
        return "(" + k + ", " + v + ")";
    }
}
