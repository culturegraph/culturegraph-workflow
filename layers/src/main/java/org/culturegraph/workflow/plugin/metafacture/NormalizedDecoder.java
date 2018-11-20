package org.culturegraph.workflow.plugin.metafacture;

import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.helpers.DefaultObjectPipe;
import org.metafacture.strings.StreamUnicodeNormalizer;

import java.text.Normalizer;

public class NormalizedDecoder extends DefaultObjectPipe<String, StreamReceiver> {
    private DefaultObjectPipe<String, StreamReceiver> decoder;
    private StreamUnicodeNormalizer normalizer;

    public NormalizedDecoder(DefaultObjectPipe<String, StreamReceiver> decoder) {
        this.decoder = decoder;
        this.normalizer = new StreamUnicodeNormalizer();

        normalizer.setNormalizationForm(Normalizer.Form.NFC);
        normalizer.setNormalizeIds(true);
        normalizer.setNormalizeKeys(true);
        normalizer.setNormalizeValues(true);
    }

    @Override
    public void process(String obj) {
        decoder.process(obj);
    }

    @Override
    public void onSetReceiver() {
        decoder.setReceiver(normalizer);
        normalizer.setReceiver(getReceiver());
    }

    @Override
    public void onResetStream() {
        decoder.resetStream();
    }

    @Override
    public void onCloseStream() {
        decoder.closeStream();
    }
}
