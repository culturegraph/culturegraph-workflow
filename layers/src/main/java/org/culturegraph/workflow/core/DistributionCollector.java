package org.culturegraph.workflow.core;

import java.util.HashMap;
import java.util.Map;

public class DistributionCollector {

    private Map<String, Map<String,Integer>> distributions;

    public DistributionCollector() {
        this.distributions = new HashMap<>();
    }

    public Map<String, Map<String, Integer>> getDistributions() {
        return distributions;
    }

    public void add(String record) {
        String name = readKey(record);

        String token[] = record.split(" ");

        Map<String,Integer> dist;
        if (distributions.containsKey(name)) {
            dist = distributions.get(name);
        } else {
            dist = new HashMap<>();
        }

        int limit = (token.length - 1) / 2;
        for (int i = 0; i < limit; i++) {
            int first = 1 + (i* 2);
            int second = first + 1;

            String key = token[first];
            int value = Integer.parseInt(token[second]);

            int storedValue = dist.getOrDefault(key, 0);
            dist.put(key, storedValue + value);
        }

        distributions.put(name, dist);
    }

    private String readKey(String s) {
        int startOfGroup = s.indexOf('(');
        int endOfGroup = s.indexOf(')');
        return s.substring(startOfGroup + 1, endOfGroup);
    }
}
