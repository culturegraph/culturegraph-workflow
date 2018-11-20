package org.culturegraph.workflow.plugin.metafacture;

import org.culturegraph.workflow.core.entities.Transformer;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.helpers.DefaultObjectPipe;
import org.metafacture.strings.StringConcatenator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class ExcerptTransformer implements Transformer {
    private static final Logger LOG = LoggerFactory.getLogger(ExcerptTransformer.class);

    private DefaultObjectPipe<String, StreamReceiver> decoder;
    private StringConcatenator resultCollector;

    public ExcerptTransformer(DefaultObjectPipe<String, StreamReceiver> decoder, StringConcatenator resultCollector) {
        this.decoder = decoder;
        this.resultCollector = resultCollector;
    }

    @Override
    public synchronized Optional<String> transform(String s) {
        try {
            decoder.resetStream();
            decoder.process(s);
            decoder.closeStream();
        } catch (Exception e) {
            LOG.error("Could not process '{}'. Error: {}", s, e.getMessage());
            return Optional.empty();
        }
        return Optional.of(resultCollector.getString());
    }
}
