package org.culturegraph.workflow.core;

import org.junit.Test;

import java.util.Map;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertThat;

public class DistributionCollectorTest {

    @Test
    public void accumulate() {
        String record = "(DE-101)123 key1 1 key2 1 key3 1";
        String record2 = "(DE-101)123 key2 1";

        DistributionCollector dc = new DistributionCollector();
        dc.add(record);
        dc.add(record2);

        Map<String, Map<String,Integer>> namedDists = dc.getDistributions();
        assertThat(namedDists, hasKey("DE-101"));

        Map<String,Integer> dist = namedDists.get("DE-101");
        assertThat(dist, hasEntry("key1", 1));
        assertThat(dist, hasEntry("key2", 2));
        assertThat(dist, hasEntry("key3", 1));
    }
}