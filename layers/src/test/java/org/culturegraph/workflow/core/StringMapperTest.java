package org.culturegraph.workflow.core;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class StringMapperTest {

    @Test
    public void mapTermInParentheses() {
        Map<String,String> map = new HashMap<>();
        map.put("DE", "ger");

        StringMapper mapper = new StringMapper(map);

        String line = "(DE)001 test";

        String result = mapper.mapTermInParentheses(line);
        String expected = "(ger)001 test";

        assertThat(result, is(equalTo(expected)));
    }

    @Test
    public void doNotMapTermWithoutMapping() {
        Map<String,String> map = new HashMap<>();
        map.put("ES", "spa");

        StringMapper mapper = new StringMapper(map);

        String line = "(DE)001 test";

        String result = mapper.mapTermInParentheses(line);
        String expected = "(DE)001 test";

        assertThat(result, is(equalTo(expected)));
    }
}