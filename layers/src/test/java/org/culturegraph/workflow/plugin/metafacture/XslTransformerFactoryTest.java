package org.culturegraph.workflow.plugin.metafacture;

import org.culturegraph.workflow.core.entities.Transformer;
import org.culturegraph.workflow.core.entities.TransformerFactory;
import org.culturegraph.workflow.plugin.metafacture.XslTransformerFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class XslTransformerFactoryTest {

    private String xml = "<?xml version=\"1.1\" encoding=\"UTF-8\"?>" +
            "<records>" +
            "<record>" +
            "<title>A fancy book title</title>" +
            "<issued>2018</issued>" +
            "</record>" +
            "</records>";
    private String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<covers>" +
            "<cover>A fancy book title</cover>" +
            "</covers>";
    private TransformerFactory factory;

    @Before
    public void setUp() {
        String filter = getClass().getClassLoader().getResource("filter.xsl").getFile();
        String rename = getClass().getClassLoader().getResource("rename.xsl").getFile();
        List<String> xslList = Stream.of(filter, rename).collect(Collectors.toList());
        factory = new XslTransformerFactory(xslList);
    }

    @Test
    public void transformSingleRecord() {
        Transformer transformer = factory.newTransformer();
        Optional<String> result = transformer.transform(xml);
        assertThat(result.isPresent(), is(true));
        assertThat(expectedXml, equalTo(result.get()));
    }

    @Test
    public void transformTwoRecords() {
        Transformer transformer = factory.newTransformer();
        Optional<String> firstResult = transformer.transform(xml);
        assertThat(firstResult.isPresent(), is(true));
        assertThat(expectedXml, equalTo(firstResult.get()));

        Optional<String> secondResult = transformer.transform(xml);
        assertThat(secondResult.isPresent(), is(true));
        assertThat(expectedXml, equalTo(secondResult.get()));
    }
}