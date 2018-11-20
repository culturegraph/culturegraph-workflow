package org.culturegraph.workflow.core;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class KeyValueCollectorTest {

    private String separator = ";";

    @Test
    public void processSingelton() throws Exception {
        List<String> csvLines = Arrays.asList("1;a");
        KeyValueCollector collector = new KeyValueCollector(csvLines.iterator(), separator);

        assertThat(collector.hasNext(), is(true));
        assertThat(collector.next(), is(equalTo("1 a")));
        assertThat(collector.hasNext(), is(false));
    }

    @Test
    public void processTwoSingeltons() throws Exception {
        List<String> csvLines = Arrays.asList("1;a", "2;b");
        KeyValueCollector collector = new KeyValueCollector(csvLines.iterator(), separator);

        assertThat(collector.hasNext(), is(true));
        assertThat(collector.next(), is(equalTo("1 a")));
        assertThat(collector.hasNext(), is(true));
        assertThat(collector.next(), is(equalTo("1 b")));
        assertThat(collector.hasNext(), is(false));
    }

    @Test
    public void processGroup() throws Exception {
        List<String> csvLines = Arrays.asList("1;a", "1;b");
        KeyValueCollector collector = new KeyValueCollector(csvLines.iterator(), separator);

        assertThat(collector.hasNext(), is(true));
        assertThat(collector.next(), is(equalTo("2 a b")));
        assertThat(collector.hasNext(), is(false));
    }

    @Test
    public void processTwoGroups() throws Exception {
        List<String> csvLines = Arrays.asList("1;a", "1;b", "2;c", "2;d");
        KeyValueCollector collector = new KeyValueCollector(csvLines.iterator(), separator);

        assertThat(collector.hasNext(), is(true));
        assertThat(collector.next(), is(equalTo("2 a b")));
        assertThat(collector.hasNext(), is(true));
        assertThat(collector.next(), is(equalTo("2 c d")));
        assertThat(collector.hasNext(), is(false));
    }

    @Test
    public void processGroupWithSingletonAtTheEnd() throws Exception {
        List<String> csvLines = Arrays.asList("1;a", "1;b", "2;c");
        KeyValueCollector collector = new KeyValueCollector(csvLines.iterator(), separator);

        assertThat(collector.hasNext(), is(true));
        assertThat(collector.next(), is(equalTo("2 a b")));
        assertThat(collector.hasNext(), is(true));
        assertThat(collector.next(), is(equalTo("1 c")));
        assertThat(collector.hasNext(), is(false));
    }

    @Test
    public void processGroupWithSingletonAtTheBeginning() throws Exception {
        List<String> csvLines = Arrays.asList("1;a", "2;b", "2;c");
        KeyValueCollector collector = new KeyValueCollector(csvLines.iterator(), separator);

        assertThat(collector.hasNext(), is(true));
        assertThat(collector.next(), is(equalTo("1 a")));
        assertThat(collector.hasNext(), is(true));
        assertThat(collector.next(), is(equalTo("2 b c")));
        assertThat(collector.hasNext(), is(false));
    }

    @Test(expected = NoSuchElementException.class)
    public void crashWhenNotUsingHasNextBeforeEachNextCall() throws Exception {
        List<String> csvLines = Arrays.asList("1;a", "2;b");
        KeyValueCollector collector = new KeyValueCollector(csvLines.iterator(), separator);

        collector.next();
    }
}