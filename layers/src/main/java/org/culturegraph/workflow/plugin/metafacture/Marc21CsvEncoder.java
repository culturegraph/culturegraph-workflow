package org.culturegraph.workflow.plugin.metafacture;

import org.metafacture.fix.biblio.marc21.Marc21Encoder;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.helpers.DefaultStreamPipe;
import org.metafacture.mangling.LiteralToObject;
import org.metafacture.metamorph.Metamorph;
import org.metafacture.strings.StringConcatenator;

public class Marc21CsvEncoder extends DefaultStreamPipe<ObjectReceiver<String>> {

    private Metamorph idSpecification;
    private Metamorph excerptSpecification;
    private String separator;

    private boolean isFirst;

    private StringConcatenator idConcaternator;
    private StringConcatenator recordConcatenator;

    public Marc21CsvEncoder(Metamorph idSpecification, Metamorph excerptSpecification, String separator) {
        this.idSpecification = idSpecification;
        this.excerptSpecification = excerptSpecification;
        this.separator = separator;
        this.isFirst = true;
        this.idConcaternator = new StringConcatenator();
        this.recordConcatenator = new StringConcatenator();
    }

    @Override
    public void startRecord(final String identifier) {
        if (isFirst) {
            idSpecification.setReceiver(new LiteralToObject()).setReceiver(idConcaternator);
            excerptSpecification.setReceiver(new Marc21Encoder()).setReceiver(recordConcatenator);
            isFirst = false;
        }

        idSpecification.resetStream();
        idSpecification.startRecord(identifier);

        excerptSpecification.resetStream();
        excerptSpecification.startRecord(identifier);
    }

    @Override
    public void endRecord() {
        idSpecification.endRecord();
        excerptSpecification.endRecord();

        String id = idConcaternator.getString();
        String record = recordConcatenator.getString();
        getReceiver().process(id + separator + record);
    }

    @Override
    public void startEntity(final String name) {
       idSpecification.startEntity(name);
       excerptSpecification.startEntity(name);
    }

    @Override
    public void endEntity() {
        idSpecification.endEntity();
        excerptSpecification.endEntity();
    }

    @Override
    public void literal(final String name, final String value) {
        idSpecification.literal(name, value);
        excerptSpecification.literal(name, value);
    }

    @Override
    public void onResetStream() {
        if (!isFirst) {
            idSpecification.resetStream();
            excerptSpecification.resetStream();
        }
        isFirst = true;
    }

    @Override
    public void onCloseStream() {
        idSpecification.closeStream();
        excerptSpecification.closeStream();
    }

}
