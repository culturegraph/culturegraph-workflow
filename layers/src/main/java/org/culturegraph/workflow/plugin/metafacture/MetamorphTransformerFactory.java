package org.culturegraph.workflow.plugin.metafacture;

import org.culturegraph.workflow.core.entities.InputFormat;
import org.culturegraph.workflow.core.entities.OutputFormat;
import org.culturegraph.workflow.core.entities.Transformer;
import org.culturegraph.workflow.core.entities.TransformerFactory;
import org.metafacture.biblio.marc21.Marc21Decoder;
import org.metafacture.biblio.marc21.Marc21Encoder;
import org.metafacture.contrib.csv.SimpleCsvEncoder;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.StreamPipe;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.helpers.DefaultObjectPipe;
import org.metafacture.framework.helpers.DefaultStreamPipe;
import org.metafacture.framework.helpers.ForwardingStreamPipe;
import org.metafacture.metamorph.Metamorph;
import org.metafacture.strings.StringConcatenator;
import org.metafacture.xml.SimpleXmlEncoder;

import java.util.List;

public class MetamorphTransformerFactory implements TransformerFactory {
    private InputFormat inputFormat;
    private OutputFormat outputFormat;
    private List<String> morphDefList;
    /** Flag that indicates normalization of input. Default behaviour does not normalize. */
    private boolean normalizeInput;
    /** Flag that indicates normalization of output. Default behaviour does not normalize. */
    private boolean normalizeOutput;
    /** Flag for adding XML declaration. Default behaviour adds the declaration. */
    private boolean includeXmlDeclaration;
    /** Flag for adding {@code <records>} root tag. Default behaviour adds the tag. */
    private boolean includeXmlRoot;

    public MetamorphTransformerFactory(InputFormat inputFormat, OutputFormat outputFormat, List<String> morphDefList) {
        this.inputFormat = inputFormat;
        this.outputFormat = outputFormat;
        this.morphDefList = morphDefList;

        this.normalizeInput = false;
        this.normalizeOutput = false;
        this.includeXmlDeclaration = true;
        this.includeXmlRoot = true;
    }

    public void setNormalizeInput(boolean normalizeInput)
    {
        this.normalizeInput = normalizeInput;
    }

    public void setNormalizeOutput(boolean normalizeOutput)
    {
        this.normalizeOutput = normalizeOutput;
    }

    public void setIncludeXmlDeclaration(boolean includeXmlDeclaration)
    {
        this.includeXmlDeclaration = includeXmlDeclaration;
    }

    public void setIncludeXmlRoot(boolean includeXmlRoot)
    {
        this.includeXmlRoot = includeXmlRoot;
    }

    @Override
    public Transformer newTransformer() {
        DefaultObjectPipe<String, StreamReceiver> decoder = newDecoder(inputFormat, normalizeInput);

        StreamPipe<StreamReceiver> receiver = decoder.setReceiver(new ForwardingStreamPipe());

        if (!morphDefList.isEmpty()) {
            for (String morphDef : morphDefList) {
                receiver = receiver.setReceiver(new Metamorph(morphDef));
            }
        }

        DefaultStreamPipe<ObjectReceiver<String>> encoder = newEncoder(outputFormat, normalizeOutput);
        StringConcatenator resultCollector = receiver
                .setReceiver(encoder)
                .setReceiver(new StringConcatenator());

        return new MetamorphTransformer(decoder, resultCollector);
    }

    private DefaultObjectPipe<String, StreamReceiver> newDecoder(InputFormat inputFormat, boolean normalize) {
        DefaultObjectPipe<String, StreamReceiver> decoder;
        switch (inputFormat) {
            case MARC21:
                decoder = new Marc21Decoder();
                break;
            default:
                throw new IllegalArgumentException("Unsupported input format " + inputFormat.name());
        }

        return normalize ? new NormalizedDecoder(decoder) : decoder;
    }

    private DefaultStreamPipe<ObjectReceiver<String>> newEncoder(OutputFormat outputFormat, boolean normalize) {
        DefaultStreamPipe<ObjectReceiver<String>> encoder;
        switch (outputFormat) {
            case CSV:
                SimpleCsvEncoder simpleCsvEncoder = new SimpleCsvEncoder();
                simpleCsvEncoder.setSeparator(' ');
                simpleCsvEncoder.setNoQuotes(true);
                encoder = simpleCsvEncoder;
                break;
            case MARC21:
                encoder = new Marc21Encoder();
                break;
            case XML:
                SimpleXmlEncoder simpleXmlEncoder = new SimpleXmlEncoder();
                simpleXmlEncoder.setWriteXmlHeader(includeXmlDeclaration);
                simpleXmlEncoder.setWriteRootTag(includeXmlRoot);
                encoder = simpleXmlEncoder;
                break;
            default:
                throw new IllegalArgumentException("Unsupported output format " + outputFormat.name());
        }
        return normalize ? new NormalizedEncoder(encoder) : encoder;
    }
}
