package org.culturegraph.workflow.core;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

@Deprecated
public class IterativeXmlCommentMapper implements Iterator<String> {

    private Iterator<String> mapIterator;
    private Iterator<String> xmlIterator;

    private String documentKey;
    private String documentBody;

    private String mappingKey;
    private String mappingValue;

    private String buf;

    public IterativeXmlCommentMapper(Iterator<String> xmlIterator, Iterator<String> mapIterator) {
        this.xmlIterator = xmlIterator;
        this.mapIterator = mapIterator;
        this.documentKey = "";
        this.mappingKey = "";

        preload();
    }

    @Override
    public boolean hasNext() {
        if (!documentKey.isEmpty() && !mappingKey.isEmpty()) {
            Optional<String> optional = findNext();
            if (optional.isPresent()) {
                buf = optional.get();
                return true;
            }
        }

        return false;
    }

    @Override
    public String next() {
        if (buf != null) {
            return buf;
        } else {
            throw new NoSuchElementException();
        }
    }

    private Optional<String> findNext() {
        while (!mappingKey.isEmpty() && !documentKey.isEmpty()) {

            int keyComparison = mappingKey.compareTo(documentKey);
            if (keyComparison == 0) {

                String xml = "<!-- " + mappingValue + " -->" + documentBody;
                Optional<String> result = Optional.of(xml);

                String document = nextOrEmpty(xmlIterator);
                processXml(document);
                String mapEntry = nextOrEmpty(mapIterator);
                processMapEntry(mapEntry);
                return result;

            } else if (keyComparison > 0) {

                String document = nextOrEmpty(xmlIterator);
                processXml(document);

            } else {

                String mapEntry = nextOrEmpty(mapIterator);
                processMapEntry(mapEntry);

            }
        }

        return Optional.empty();
    }

    private String nextOrEmpty(Iterator<String> iterator) {
        try {
            String nxt = iterator.next();
            return nxt == null ? "" : nxt;
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    private void processXml(String xml) {
        if (xml.isEmpty()) {
            documentKey = "";
            documentBody = "";
        } else {
            int startOfComment = 0;
            int endOfComment = xml.indexOf('>');
            // Read comment content
            // Note: '<!--' has length of 4, '-->' has length of 3
            documentKey = xml.substring(startOfComment + 4, endOfComment - 3).trim();
            documentBody = xml.substring(endOfComment + 1);
        }
    }

    private void processMapEntry(String mapEntry) {
        if (mapEntry.isEmpty()) {
            mappingKey = "";
            mappingValue = "";
        } else {
            String[] token = mapEntry.split(" ", 2);
            mappingKey = token[0];
            mappingValue = token[1];
        }
    }

    /**
     * Loads the first entry of each iterator.
     */
    private void preload() {
        String map = nextOrEmpty(mapIterator);
        processMapEntry(map);
        String document = nextOrEmpty(xmlIterator);
        processXml(document);
    }
}
