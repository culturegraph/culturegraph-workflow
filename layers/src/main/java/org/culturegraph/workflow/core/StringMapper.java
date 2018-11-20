package org.culturegraph.workflow.core;

import java.util.Map;

public class StringMapper {

    private Map<String,String> map;

    public StringMapper(Map<String,String> map) {
        this.map = map;
    }

    public String mapTermInParentheses(String s) {
        String token[] = s.trim().split("\\)", 2);
        String beforeClosing = token[0];
        String afterClosing = token[1];

        int positionOfOpening = beforeClosing.indexOf("(");
        String beforeOpening = beforeClosing.substring(0, positionOfOpening);
        String term = beforeClosing.substring(positionOfOpening + 1);

        if (map.containsKey(term)) {
            String mappedTerm = map.get(term);
            return beforeOpening + "(" + mappedTerm + ")" + afterClosing;
        }

        return s;
    }
}
