package org.culturegraph.workflow.core.entities;

public class ChainedTransformerFactory implements TransformerFactory {
    private TransformerFactory factory1;
    private TransformerFactory factory2;

    public ChainedTransformerFactory(TransformerFactory factory1, TransformerFactory factory2) {
        this.factory1 = factory1;
        this.factory2 = factory2;
    }

    @Override
    public Transformer newTransformer() {
        Transformer transformer1 = factory1.newTransformer();
        Transformer transformer2 = factory2.newTransformer();
        return new ChainedTransformer(transformer1, transformer2);
    }
}
