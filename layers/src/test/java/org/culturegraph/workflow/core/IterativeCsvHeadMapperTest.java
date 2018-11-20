package org.culturegraph.workflow.core;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class IterativeCsvHeadMapperTest {

    private String separator = ";";

    @Test
    public void shouldMatch() {
        List<String> mapEntryList = Arrays.asList("key value");
        List<String> csvLines = Arrays.asList("key;record");

        IterativeCsvHeadMapper mapper = new IterativeCsvHeadMapper(csvLines.iterator(), separator, mapEntryList.iterator());

        String mappedCsv = "value;record";

        assertThat(mapper.hasNext(), is(true));
        assertThat(mapper.next(), is(equalTo(mappedCsv)));
        assertThat(mapper.hasNext(), is(false));
    }

    @Test
    public void shouldNoMatch() {
        List<String> mapEntryList = Arrays.asList("key value");
        List<String> csvLines = Arrays.asList("key1;record");

        IterativeCsvHeadMapper mapper = new IterativeCsvHeadMapper(csvLines.iterator(), separator, mapEntryList.iterator());

        assertThat(mapper.hasNext(), is(false));
    }

    @Test
    public void shouldMapFirstDocumentInSequence() {
        List<String> mapEntryList = Arrays.asList("1 hit");
        List<String> csvLines = Arrays.asList(
                "1;record1",
                "2;record2");

        IterativeCsvHeadMapper mapper = new IterativeCsvHeadMapper(csvLines.iterator(), separator, mapEntryList.iterator());

        String mappedDocument = "hit;record1";

        assertThat(mapper.hasNext(), is(true));
        assertThat(mapper.next(), is(equalTo(mappedDocument)));
        assertThat(mapper.hasNext(), is(false));
    }

    @Test
    public void shouldMapSecondDocumentInSequence() {
        List<String> mapEntryList = Arrays.asList("2 hit");
        List<String> csvLines = Arrays.asList(
                "1;record1",
                "2;record2",
                "3;record3");

        IterativeCsvHeadMapper mapper = new IterativeCsvHeadMapper(csvLines.iterator(), separator, mapEntryList.iterator());

        String mappedDocument = "hit;record2";

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
        List<String> csvLines = Arrays.asList(
                "1;record1",
                "2;record2",
                "4;record4");

        IterativeCsvHeadMapper mapper = new IterativeCsvHeadMapper(csvLines.iterator(), separator, mapEntryList.iterator());

        assertThat(mapper.hasNext(), is(true));
        assertThat(mapper.next(), is(equalTo("hit;record2")));
        assertThat(mapper.hasNext(), is(true));
        assertThat(mapper.next(), is(equalTo("hit;record4")));
        assertThat(mapper.hasNext(), is(false));
    }
}