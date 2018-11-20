package org.culturegraph.workflow.plugin.io;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.marc.Record;

public class Marc21Converter {

    private static ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private static MarcWriter writer = new MarcStreamWriter(outputStream, "UTF-8");

    public static String convert(Record record) {
        outputStream.reset();
        writer.write(record);
        byte[] bytes = outputStream.toByteArray();
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
