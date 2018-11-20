package org.culturegraph.workflow.core;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@Deprecated
public class IterativeXmlCommentMapperTest {

    @Test
    public void shouldMatch() {
        List<String> mapEntryList = Arrays.asList("A B");
        List<String> xmlDocumentList = Arrays.asList("<!-- A --><record></record>");

        IterativeXmlCommentMapper mapper = new IterativeXmlCommentMapper(
                xmlDocumentList.iterator(),
                mapEntryList.iterator()
        );

        String mappedDocument = "<!-- B --><record></record>";

        assertThat(mapper.hasNext(), is(true));
        assertThat(mapper.next(), is(equalTo(mappedDocument)));
        assertThat(mapper.hasNext(), is(false));
    }

    @Test
    public void shouldNoMatch() {
        List<String> mapEntryList = Arrays.asList("A B");
        List<String> xmlDocumentList = Arrays.asList("<!-- X --><record></record>");

        IterativeXmlCommentMapper mapper = new IterativeXmlCommentMapper(
                xmlDocumentList.iterator(),
                mapEntryList.iterator()
        );

        assertThat(mapper.hasNext(), is(false));
    }

    @Test
    public void shouldMapFirstDocumentInSequence() {
        List<String> mapEntryList = Arrays.asList("1 hit");
        List<String> xmlDocumentList = Arrays.asList(
                "<!-- 1 --><record>1</record>",
                "<!-- 2 --><record>2</record>");

        IterativeXmlCommentMapper mapper = new IterativeXmlCommentMapper(
                xmlDocumentList.iterator(),
                mapEntryList.iterator()
        );

        String mappedDocument = "<!-- hit --><record>1</record>";

        assertThat(mapper.hasNext(), is(true));
        assertThat(mapper.next(), is(equalTo(mappedDocument)));
        assertThat(mapper.hasNext(), is(false));
    }

    @Test
    public void shouldMapSecondDocumentInSequence() {
        List<String> mapEntryList = Arrays.asList("2 hit");
        List<String> xmlDocumentList = Arrays.asList(
                "<!-- 1 --><record>1</record>",
                "<!-- 2 --><record>2</record>",
                "<!-- 3 --><record>3</record>");

        IterativeXmlCommentMapper mapper = new IterativeXmlCommentMapper(
                xmlDocumentList.iterator(),
                mapEntryList.iterator()
        );

        String mappedDocument = "<!-- hit --><record>2</record>";

        assertThat(mapper.hasNext(), is(true));
        assertThat(mapper.next(), is(equalTo(mappedDocument)));
        assertThat(mapper.hasNext(), is(false));
    }

    @Test
    public void shouldMapSecondAndFourthDocumentInSequence() {
        List<String> mapEntryList = Arrays.asList(
                "2 hit",
                "3 hit",
                "4 hit");
        List<String> xmlDocumentList = Arrays.asList(
                "<!-- 1 --><record>1</record>",
                "<!-- 2 --><record>2</record>",
                "<!-- 4 --><record>4</record>");

        IterativeXmlCommentMapper mapper = new IterativeXmlCommentMapper(
                xmlDocumentList.iterator(),
                mapEntryList.iterator()
        );

        assertThat(mapper.hasNext(), is(true));
        assertThat(mapper.next(), is(equalTo("<!-- hit --><record>2</record>")));
        assertThat(mapper.hasNext(), is(true));
        assertThat(mapper.next(), is(equalTo("<!-- hit --><record>4</record>")));
        assertThat(mapper.hasNext(), is(false));
    }
}