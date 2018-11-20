package org.culturegraph.workflow.core;

public class BundleBuilder {
    public String algorithm;

    private String documentKey;
    private String documentBody;

    private String lastDocumentKey;
    private boolean isFirst = true;

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

    public BundleBuilder() {
        this("unknown");
    }

    public BundleBuilder(String algorithm) {
        this.algorithm = algorithm;
    }

    private void processXml(String xml) {
        int endOfComment = xml.indexOf('>');
        int startOfComment = 0;
        documentKey = xml.substring(startOfComment + 4, endOfComment - 3).trim();
        documentBody = xml.substring(endOfComment + 1);
    }

    public String open() {
        return XML_DECLARATION + "\n" + ROOT_START;
    }

    public String put(String document) {

        processXml(document);

        String result;

        if (documentKey.equalsIgnoreCase(lastDocumentKey)) {
            result = documentBody;
        } else {
            if (isFirst) {
                isFirst = false;
                result = String.format(BUNDLE_START_TEMPLATE, "cl-" + algorithm + documentKey, algorithm) +
                        "\n" +
                        documentBody;
            } else {
                result = BUNDLE_END +
                        "\n" +
                        String.format(BUNDLE_START_TEMPLATE, "cl-" + algorithm + documentKey, algorithm) +
                        "\n" +
                        documentBody;
            }
        }

        lastDocumentKey = documentKey;

        return result;
    }

    public String close() {
        return BUNDLE_END + "\n" + ROOT_END + "\n";
    }
}
