package org.culturegraph.workflow.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class KeyValueCollector implements Iterator<String> {

    private String separator;
    private Iterator<String> keyValueIterator;
    private String lastKey;
    private List<String> row;
    private StringBuffer buffer;

    public KeyValueCollector(Iterator<String> keyValueIterator, String separator) {
        this.separator = separator;
        this.keyValueIterator = keyValueIterator;
        this.row = new ArrayList<>();
        this.buffer = new StringBuffer();
        this.lastKey = "";
    }

    @Override
    public boolean hasNext() {
        while (keyValueIterator.hasNext()) {
            String pair = keyValueIterator.next();
            String[] kv = pair.split(separator, 2);

            if (kv.length != 2) {
                throw new NoSuchElementException("Could not split line " + "'" + pair + "'" + " with separator " + "'" + separator + "'");
            }

            String key = kv[0];
            String value = kv[1];

            if (lastKey.isEmpty()) {
                lastKey = key;
            }

            if (!lastKey.equals(key)) {
                lastKey = key;

                populateBuffer(row);

                // Start new row
                row = new ArrayList<>();
                row.add(value);

                return true;
            } else {
                row.add(value);
            }
        }

        if (!row.isEmpty()) {
            populateBuffer(row);
            row = new ArrayList<>();
            return true;
        }

        return false;
    }

    @Override
    public String next() {
        if (buffer.length() == 0) {
            throw new NoSuchElementException();
        }

        String result = buffer.toString();

        buffer = new StringBuffer();
        return result;
    }

    private void populateBuffer(List<String> row) {
        int size = row.size();
        buffer.append(size);
        buffer.append(" ");

        int pos = 0;
        for (String item: row) {
            pos += 1;
            buffer.append(item);
            if (pos != size) buffer.append(" ");
        }
    }
}
