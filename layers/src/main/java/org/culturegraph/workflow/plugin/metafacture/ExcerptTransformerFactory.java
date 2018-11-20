package org.culturegraph.workflow.plugin.metafacture;

import org.culturegraph.workflow.core.entities.Transformer;
import org.culturegraph.workflow.core.entities.TransformerFactory;
import org.metafacture.biblio.marc21.Marc21Decoder;
import org.metafacture.fix.biblio.marc21.Marc21Encoder;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.StreamPipe;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.helpers.DefaultObjectPipe;
import org.metafacture.framework.helpers.DefaultStreamPipe;
import org.metafacture.framework.helpers.ForwardingStreamPipe;
import org.metafacture.metamorph.Metamorph;
import org.metafacture.strings.StringConcatenator;

public class ExcerptTransformerFactory implements TransformerFactory {
    /** Metamorph definition file for id generation */
    private String idMorphDef;
    /** Metamorph definition file for record excerpt */
    private String excerptMorphDef;
    /** Flag that indicates normalization of input. Default behaviour does not normalize. */
    private boolean normalizeInput;
    /** Flag that indicates normalization of output. Default behaviour does not normalize. */
    private boolean normalizeOutput;

    private String separator;

    public ExcerptTransformerFactory(String separator, String idMorphDef, String excerptMorphDef) {
        this.idMorphDef = idMorphDef;
        this.excerptMorphDef = excerptMorphDef;
        this.separator = separator;

        this.normalizeInput = false;
        this.normalizeOutput = false;
    }

    public void setNormalizeInput(boolean normalizeInput)
    {
        this.normalizeInput = normalizeInput;
    }

    public void setNormalizeOutput(boolean normalizeOutput)
    {
        this.normalizeOutput = normalizeOutput;
    }

    @Override
    public Transformer newTransformer() {
        DefaultObjectPipe<String, StreamReceiver> decoder = newDecoder(normalizeInput);
        StreamPipe<StreamReceiver> receiver = decoder.setReceiver(new ForwardingStreamPipe());

        Metamorph idSpec = new Metamorph(idMorphDef);
        Metamorph excerptSpec = new Metamorph(excerptMorphDef);


        DefaultStreamPipe<ObjectReceiver<String>> encoder = new Marc21CsvEncoder(idSpec, excerptSpec, separator);
        if (normalizeOutput) encoder = new NormalizedEncoder(encoder);

        StringConcatenator resultCollector = receiver
                .setReceiver(encoder)
                .setReceiver(new StringConcatenator());

        return new ExcerptTransformer(decoder, resultCollector);
    }

    private DefaultObjectPipe<String, StreamReceiver> newDecoder(boolean normalize) {
        DefaultObjectPipe<String, StreamReceiver> decoder = new Marc21Decoder();

        return normalize ? new NormalizedDecoder(decoder) : decoder;
    }

    private DefaultStreamPipe<ObjectReceiver<String>> newEncoder(boolean normalize) {
        DefaultStreamPipe<ObjectReceiver<String>> encoder = new Marc21Encoder();
        return normalize ? new NormalizedEncoder(encoder) : encoder;
    }
}
