package org.culturegraph.workflow.core.entities;

import java.util.Optional;

public interface Transformer {
    Optional<String> transform(String s);
}
