package org.culturegraph.workflow.plugin.io;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.marc4j.MarcReader;
import org.marc4j.marc.Record;

public class MarcReaderWrapper implements Iterable<Record> {
    private MarcReader reader;

    public MarcReaderWrapper(MarcReader reader) {
        this.reader = reader;
    }

    public Stream<Record> records() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                this.iterator(), Spliterator.ORDERED | Spliterator.NONNULL), false);
    }

    @Override
    public Iterator<Record> iterator() {
        return new Iterator<Record>() {
            @Override
            public boolean hasNext() {
                return reader.hasNext();
            }

            @Override
            public Record next() {
                return reader.next();
            }
        };
    }
}
