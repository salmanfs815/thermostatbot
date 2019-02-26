package com.redhat.thermostat.bot;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Utils {

    /**
     * Find matching occurrences of {@code pattern} within {@code text} and
     * return the specified {@code group}.
     */
    public static Set<String> getMatches(String text, Pattern pattern, int group) {
        Set<String> result = new HashSet<>();
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String match = matcher.group(group);
            result.add(match);
        }
        return result;
    }

}
