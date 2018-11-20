package org.culturegraph.workflow.learning;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PushbackInputStreamTest {

    private Charset utf8 = StandardCharsets.UTF_8;

    @Test
    public void checkFirstCharacterOfStream() throws Exception {

        String record = "ok";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(record.getBytes(utf8));

        PushbackInputStream pushbackInputStream = new PushbackInputStream(inputStream, 1);
        byte[] signature = new byte[1];

        int length = pushbackInputStream.read(signature);
        pushbackInputStream.unread(signature, 0, length);

        char firstChar = (char) signature[0];

        assertThat(firstChar, is(equalTo('o')));
    }

    @Test
    public void checkFirstCharacterOfCompressedStream() throws Exception {
        String record = "ok";

        // Write compress stream
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (OutputStream outputStream = new GZIPOutputStream(buffer)) {
            outputStream.write(record.getBytes(utf8));
        }

        ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer.toByteArray());

        // Check if stream is compressed
        byte[] magic = new byte[2];
        inputStream.read(magic, 0, 2);
        assertThat(magic[0] == (byte) (GZIPInputStream.GZIP_MAGIC), is(true));
        assertThat(magic[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8), is(true));

        inputStream = new ByteArrayInputStream(buffer.toByteArray());
        GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);

        // Check if uncompressed stream starts with char 'o'
        PushbackInputStream pushbackInputStream = new PushbackInputStream(gzipInputStream, 1);
        byte[] signature = new byte[1];

        int length = pushbackInputStream.read(signature);
        pushbackInputStream.unread(signature, 0, length);

        char firstChar = (char) signature[0];
        assertThat(firstChar, is(equalTo('o')));

        // Check if push back was successful
        char firstCharAfterPushback = (char) pushbackInputStream.read();
        assertThat(firstCharAfterPushback, is(equalTo('o')));
    }
}
