package org.culturegraph.workflow.core;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

public class IterativeCsvHeadMapper implements Iterator<String> {

    private Iterator<String> mapIterator;
    private Iterator<String> csvIterator;

    private String rowKey;
    private String rowRecord;

    private String mappingKey;
    private String mappingValue;

    private String buf;
    private String separator;

    public IterativeCsvHeadMapper(Iterator<String> csvIterator, String separator, Iterator<String> mapIterator) {
        this.csvIterator = csvIterator;
        this.separator = separator;
        this.mapIterator = mapIterator;
        this.rowKey = "";
        this.mappingKey = "";

        preload();
    }

    @Override
    public boolean hasNext() {
        if (!rowKey.isEmpty() && !mappingKey.isEmpty()) {
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
        while (!mappingKey.isEmpty() && !rowKey.isEmpty()) {

            int keyComparison = mappingKey.compareTo(rowKey);
            if (keyComparison == 0) {

                String csvRow = mappingValue + separator + rowRecord;
                Optional<String> result = Optional.of(csvRow);

                String csv = nextOrEmpty(csvIterator);
                processCsv(csv);
                String mapEntry = nextOrEmpty(mapIterator);
                processMapEntry(mapEntry);
                return result;

            } else if (keyComparison > 0) {

                String csv = nextOrEmpty(csvIterator);
                processCsv(csv);

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

    private void processCsv(String csv) {
        if (csv.isEmpty()) {
            rowKey = "";
            rowRecord = "";
        } else {
            String[] rows = csv.split(separator, 2);
            rowKey = rows[0].trim();
            rowRecord = rows[1];
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
        String document = nextOrEmpty(csvIterator);
        processCsv(document);
    }
}
