package org.culturegraph.workflow.plugin.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.Record;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class Marc21ConverterTest {

    private MarcReader reader;
    private Marc21Converter converter;

    @Before
    public void setUp() throws Exception {
        converter = new Marc21Converter();

        String collection = declaration +collectionStart + marcxml1 + marcxml2 + collectionEnd;
        byte[] bytes = collection.getBytes(utf8);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

        reader = new MarcXmlReader(inputStream);
    }

    @Test
    public void convert() {
        assertThat(reader.hasNext(), is(true));
        Record rec1 = reader.next();
        assertThat(asMarc21(marcxml1), is(equalTo(converter.convert(rec1))));

        assertThat(reader.hasNext(), is(true));
        Record rec2 = reader.next();
        assertThat(asMarc21(marcxml2), is(equalTo(converter.convert(rec2))));
    }

    private String asMarc21(String marcxml) {
        Charset utf8 = StandardCharsets.UTF_8;
        byte[] bytes = marcxml.getBytes(utf8);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        MarcReader reader = new MarcXmlReader(new ByteArrayInputStream(bytes));
        MarcWriter writer = new MarcStreamWriter(outputStream, "UTF-8");
        while (reader.hasNext()) {
            writer.write(reader.next());
        }

        return new String(outputStream.toByteArray(), utf8);
    }

    private Charset utf8 = StandardCharsets.UTF_8;

    private final String marcxml1 = "<marc:record xmlns:marc=\"http://www.loc.gov/MARC21/slim\">\n" +
            "          <marc:leader>00000nam a2200000zc 4500</marc:leader>\n" +
            "          <marc:controlfield tag=\"001\">BV018229257</marc:controlfield>\n" +
            "          <marc:controlfield tag=\"003\">DE-604</marc:controlfield>\n" +
            "          <marc:controlfield tag=\"005\">20050722</marc:controlfield>\n" +
            "          <marc:controlfield tag=\"007\">t</marc:controlfield>\n" +
            "          <marc:controlfield tag=\"008\">040417s1961             |||| 00||| bel d</marc:controlfield>\n" +
            "          <marc:datafield tag=\"035\" ind1=\" \" ind2=\" \">\n" +
            "            <marc:subfield code=\"a\">(OCoLC)163791912</marc:subfield>\n" +
            "          </marc:datafield>\n" +
            "          <marc:datafield tag=\"035\" ind1=\" \" ind2=\" \">\n" +
            "            <marc:subfield code=\"a\">(DE-599)BVBBV018229257</marc:subfield>\n" +
            "          </marc:datafield>\n" +
            "        </marc:record>";

    private final String marcxml2 = "<marc:record xmlns:marc=\"http://www.loc.gov/MARC21/slim\">\n" +
            "          <marc:leader>00000nam a2200000zc 4500</marc:leader>\n" +
            "          <marc:controlfield tag=\"001\">BV018229257</marc:controlfield>\n" +
            "          <marc:controlfield tag=\"003\">DE-604</marc:controlfield>\n" +
            "        </marc:record>";

    private final String declaration = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";

    private final String collectionStart = "<marc:collection " +
            "xmlns:marc=\"http://www.loc.gov/MARC21/slim\" "+
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "xsi:schemaLocation=\"http://www.loc.gov/MARC21/slim " +
            "http://www.loc.gov/standards/marcxml/schema/MARC21slim.xsd\">";

    private final String collectionEnd = "</marc:collection>";
}