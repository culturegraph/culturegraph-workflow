package org.culturegraph.workflow.core.entities;

public class IdentityTransformerFactory implements TransformerFactory
{
    @Override
    public Transformer newTransformer()
    {
        return new IdentityTransformer();
    }
}
