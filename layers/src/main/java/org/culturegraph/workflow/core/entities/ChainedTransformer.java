package org.culturegraph.workflow.core.entities;

import java.util.Optional;

public class ChainedTransformer implements Transformer {

    private Transformer transformer1;
    private Transformer transformer2;

    public ChainedTransformer(Transformer transformer1, Transformer transformer2) {
        this.transformer1 = transformer1;
        this.transformer2 = transformer2;
    }

    @Override
    public Optional<String> transform(String s) {
        Optional<String> result = transformer1.transform(s).flatMap(s2 -> transformer2.transform(s2));
        return result;
    }
}
