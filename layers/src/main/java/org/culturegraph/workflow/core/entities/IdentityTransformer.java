package org.culturegraph.workflow.core.entities;

import java.util.Optional;

public class IdentityTransformer implements Transformer
{
    @Override
    public Optional<String> transform(String s)
    {
        return Optional.of(s);
    }
}
