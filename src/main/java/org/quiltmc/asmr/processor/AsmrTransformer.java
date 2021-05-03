package org.quiltmc.asmr.processor;

import java.util.List;

public interface AsmrTransformer {
    /**
     * Returns a list of phases that this transformer can apply in, in order of preference.
     * The first phase in this list that actually exists will be picked.
     *
     * Null values represents that the transformer should not be applied. The null phase is considered to always exist
     * (along with the set of standard phases). An exception will be thrown if none of the returned phases exist.
     */
    List<String> getPhases();

    void addDependencies(AsmrProcessor processor);

    void read(AsmrProcessor processor);
}
