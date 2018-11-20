package org.culturegraph.workflow.plugin.metafacture;

import org.culturegraph.workflow.core.entities.Transformer;
import org.culturegraph.workflow.core.entities.TransformerFactory;
import org.metafacture.contrib.framework.XdmPipe;
import org.metafacture.contrib.framework.XdmReceiver;
import org.metafacture.contrib.framework.helpers.DefaultXdmPipe;
import org.metafacture.contrib.framework.helpers.ForwardingXdmPipe;
import org.metafacture.contrib.xdm.SaxToXdm;
import org.metafacture.contrib.xdm.XdmTransformationEncoder;
import org.metafacture.contrib.xdm.XdmTransformer;
import org.metafacture.contrib.xdm.XdmXmlEncoder;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.strings.StringConcatenator;
import org.metafacture.xml.XmlDecoder;

import java.util.List;

public class XslTransformerFactory implements TransformerFactory {

    private List<String> xslStylesheets;

    public XslTransformerFactory(List<String> xslStylesheets) {
        this.xslStylesheets = xslStylesheets;
    }

    @Override
    public Transformer newTransformer() {
        XmlDecoder decoder = new XmlDecoder();

        XdmPipe<XdmReceiver> receiver = decoder
                .setReceiver(new SaxToXdm())
                .setReceiver(new ForwardingXdmPipe());

        int size = xslStylesheets.size();
        if (size > 1) {
            for (String stylesheet : xslStylesheets.subList(0, size - 1)) {
                receiver = receiver.setReceiver(new XdmTransformer(stylesheet));
            }
        }

        DefaultXdmPipe<ObjectReceiver<String>> encoder;
        if (size > 0) {
            encoder = new XdmTransformationEncoder(xslStylesheets.get(size - 1));
        } else {
            encoder = new XdmXmlEncoder();
        }

        StringConcatenator resultCollector = receiver
                .setReceiver(encoder)
                .setReceiver(new StringConcatenator());

        return new XslTransformer(decoder, resultCollector);
    }
}
