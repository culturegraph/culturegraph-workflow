package org.culturegraph.workflow.core;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class BundleBuilderTest {

    @Test
    public void builderOpen() {
        BundleBuilder builder = new BundleBuilder();
        assertThat(withoutNewlines(builder.open()), is(equalTo(XML_DECLARATION + ROOT_START)));
    }

    @Test
    public void buildSingleCluster() {
        String rec1 = "<!-- 1 --><marc21-excerpt>1</marc21-excerpt>";
        String rec2 = "<!-- 1 --><marc21-excerpt>2</marc21-excerpt>";
        String rec3 = "<!-- 1 --><marc21-excerpt>3</marc21-excerpt>";
        List<String> records = Arrays.asList(rec1, rec2, rec3);

        BundleBuilder builder = new BundleBuilder("test");

        StringJoiner buf = new StringJoiner("");
        for (String record: records) {
            buf.add(builder.put(record));
        }

        String expected = String.format(BUNDLE_START_TEMPLATE, "cl-test1", "test") +
                stripLeadingComment(rec1) +
                stripLeadingComment(rec2) +
                stripLeadingComment(rec3);

        assertThat(expected, is(equalTo(withoutNewlines(buf.toString()))));
    }

    @Test
    public void buildMultipleCluster() {
        String rec1 = "<!-- 1 --><marc21-excerpt>1</marc21-excerpt>";
        String rec2 = "<!-- 2 --><marc21-excerpt>2</marc21-excerpt>";
        String rec3 = "<!-- 2 --><marc21-excerpt>3</marc21-excerpt>";
        String rec4 = "<!-- 3 --><marc21-excerpt>4</marc21-excerpt>";
        List<String> records = Arrays.asList(rec1, rec2, rec3, rec4);

        BundleBuilder builder = new BundleBuilder("test");

        StringJoiner buf = new StringJoiner("");
        for (String record: records) {
            buf.add(builder.put(record));
        }

        String expected = String.format(BUNDLE_START_TEMPLATE, "cl-test1", "test") +
                stripLeadingComment(rec1) +
                BUNDLE_END +
                String.format(BUNDLE_START_TEMPLATE, "cl-test2", "test") +
                stripLeadingComment(rec2) +
                stripLeadingComment(rec3) +
                BUNDLE_END +
                String.format(BUNDLE_START_TEMPLATE, "cl-test3", "test") +
                stripLeadingComment(rec4);

        assertThat(expected, is(equalTo(withoutNewlines(buf.toString()))));
    }

    @Test
    public void builderClose() {
        BundleBuilder builder = new BundleBuilder();
        assertThat(withoutNewlines(builder.close()), is(equalTo(BUNDLE_END + ROOT_END)));
    }


    private String withoutNewlines(String s) {
        return s.replaceAll("\n", "");
    }

    /**
     * Removes the leading comment.
     * @return
     */
    private String stripLeadingComment(String inlineXml) {
        return inlineXml.split("-->", 2)[1];
    }


    private final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private final String ROOT_START = "<bundles" +
            " xmlns=\"http://culturegraph.org/bundles\"" +
            " xmlns:marc21=\"http://culturegraph.org/MARC21fragment\"" +
            " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
            " xsi:schemaLocation=\"http://culturegraph.org/bundles bundles.xsd" +
            " http://culturegraph.org/MARC21fragment MARC21fragment.xsd\"" +
            ">";
    private final String ROOT_END = "</bundles>";
    private final String BUNDLE_START_TEMPLATE = "<bundle" +
            " ref=\"http://hub.culturegraph.org/resource/%s\"" +
            " algorithm=\"http://hub.culturegraph.org/statistics/alg/%s\"" +
            ">";
    private final String BUNDLE_END = "</bundle>";
}