package org.culturegraph.workflow.plugin.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.stream.Stream;

import org.marc4j.MarcXmlReader;

public class MarcStreamFactory {
    /**
     * Reads a input stream (marc21 or marcxml) and produces a stream of marc21 string-records.
     * Check if the input stream is marcxml by checking if the first character equals '<'.
     *
     * @throws IOException if the input stream content discovery fails.
     */
    public static Stream<String> newRecordStream(InputStream inputStream) throws IOException {
        PushbackInputStream pushbackInputStream = new PushbackInputStream(DecompressedInputStream.of(inputStream), 1);
        byte[] signature = new byte[1];
        int length = pushbackInputStream.read(signature);
        pushbackInputStream.unread(signature, 0, length);
        char firstChar = (char) signature[0];
        boolean isXml = firstChar == '<';

        if (isXml) {
            MarcXmlReader reader = new MarcXmlReader(pushbackInputStream);
            MarcReaderWrapper wrapper = new MarcReaderWrapper(reader);
            return wrapper.records().map(Marc21Converter::convert);
        } else {
            ChunkReader reader = new ChunkReader(pushbackInputStream, "\u001D");
            return reader.records();
        }
    }

    public static Stream<String> newRecordStream(File f) throws IOException {
        return newRecordStream(new FileInputStream(f));
    }
}
