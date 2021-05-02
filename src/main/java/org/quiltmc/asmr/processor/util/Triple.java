package org.quiltmc.asmr.processor.util;

public class Triple<K, M, V> {
    public final K k;
    public final M m;
    public final V v;

    public static <K, M, V> Triple<K, M, V> of(K k, M m, V v) {
        return new Triple<>(k, m, v);
    }

    private Triple(K k, M m, V v) {
        this.k = k;
        this.m = m;
        this.v = v;
    }

    @Override
    public int hashCode() {
        return 31 * (31 * k.hashCode() + m.hashCode()) + v.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Triple)) return false;
        Triple<?, ?, ?> that = (Triple<?, ?, ?>) obj;
        return k.equals(that.k) && m.equals(that.m) && v.equals(that.v);
    }

    @Override
    public String toString() {
        return "(" + k + ", " + m + ", " + v + ")";
    }
}
