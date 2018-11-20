package org.culturegraph.workflow.plugin.metafacture;

import org.culturegraph.workflow.core.entities.InputFormat;
import org.culturegraph.workflow.core.entities.OutputFormat;
import org.culturegraph.workflow.core.entities.Transformer;
import org.culturegraph.workflow.core.entities.TransformerFactory;
import org.culturegraph.workflow.plugin.metafacture.MetamorphTransformerFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MetamorphTransformerFactoryTest {

    private String marc21 = "00081pam a2200061 c 4500001001000000003000700010007000200017\u001E" +
            "000000000\u001E" +
            "DE-101\u001E" +
            "t\u001E" +
            "\u001D";
    private String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<records>" +
            "<record><code>DE-101</code></record>" +
            "</records>";
    private TransformerFactory factory;

    @Before
    public void setUp() {
        String filter = getClass().getClassLoader().getResource("filter.xml").getFile();
        String rename = getClass().getClassLoader().getResource("rename.xml").getFile();
        List<String> morphDefs = Stream.of(filter, rename).collect(Collectors.toList());
        factory = new MetamorphTransformerFactory(InputFormat.MARC21, OutputFormat.XML, morphDefs);
    }

    @Test
    public void transformSingleRecord() {
        Transformer transformer = factory.newTransformer();
        Optional<String> result = transformer.transform(marc21);

        assertThat(result.isPresent(), is(true));
        assertThat(xml, equalTo(withoutNewlineOrTabulator(result.get())));
    }

    @Test
    public void transformTwoRecords() {
        Transformer transformer = factory.newTransformer();
        Optional<String> firstResult = transformer.transform(marc21);
        assertThat(firstResult.isPresent(), is(true));
        assertThat(xml, equalTo(withoutNewlineOrTabulator(firstResult.get())));

        Optional<String> secondResult = transformer.transform(marc21);
        assertThat(secondResult.isPresent(), is(true));
        assertThat(xml, equalTo(withoutNewlineOrTabulator(secondResult.get())));
    }

    private String withoutNewlineOrTabulator(String s) {
        return s.replaceAll("[\n\t]", "");
    }
}