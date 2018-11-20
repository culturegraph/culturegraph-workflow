package org.culturegraph.workflow.plugin.metafacture;

import org.culturegraph.workflow.core.entities.Transformer;
import org.metafacture.strings.StringConcatenator;
import org.metafacture.xml.XmlDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.Optional;

public class XslTransformer implements Transformer {
    private static final Logger LOG = LoggerFactory.getLogger(XslTransformer.class);

    private XmlDecoder decoder;
    private StringConcatenator resultCollector;

    public XslTransformer(XmlDecoder decoder, StringConcatenator resultCollector) {
        this.decoder = decoder;
        this.resultCollector = resultCollector;
    }

    @Override
    public synchronized Optional<String> transform(String s) {
        try {
            decoder.resetStream();
            decoder.process(new StringReader(s));
        } catch (Exception e) {
            LOG.error("Could not process '{}'. Error: {}", s, e.getMessage());
            return Optional.empty();
        }
        return Optional.of(resultCollector.getString());
    }
}
