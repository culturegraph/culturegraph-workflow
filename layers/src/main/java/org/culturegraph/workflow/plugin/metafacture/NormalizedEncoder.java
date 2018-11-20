package org.culturegraph.workflow.plugin.metafacture;

import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.helpers.DefaultStreamPipe;
import org.metafacture.strings.StreamUnicodeNormalizer;

import java.text.Normalizer;

public class NormalizedEncoder extends DefaultStreamPipe<ObjectReceiver<String>> {
    private DefaultStreamPipe<ObjectReceiver<String>> encoder;
    private StreamUnicodeNormalizer normalizer;

    public NormalizedEncoder(DefaultStreamPipe<ObjectReceiver<String>> encoder) {
        this.encoder = encoder;
        this.normalizer = new StreamUnicodeNormalizer();

        normalizer.setNormalizationForm(Normalizer.Form.NFC);
        normalizer.setNormalizeIds(true);
        normalizer.setNormalizeKeys(true);
        normalizer.setNormalizeValues(true);
    }

    @Override
    public void onSetReceiver() {
        normalizer.setReceiver(encoder);
        encoder.setReceiver(getReceiver());
    }

    @Override
    public void startRecord(String identifier) {
        normalizer.startRecord(identifier);
    }

    @Override
    public void endRecord() {
        normalizer.endRecord();
    }

    @Override
    public void startEntity(String name) {
        normalizer.startEntity(name);
    }

    @Override
    public void endEntity() {
        normalizer.endEntity();
    }

    @Override
    public void literal(String name, String value) {
        normalizer.literal(name, value);
    }

    @Override
    public void onResetStream() {
        normalizer.resetStream();
    }

    @Override
    public void onCloseStream() {
        normalizer.closeStream();
    }
}
