package com.redhat.thermostat.bot;

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TimeZoneHelper {
    private static Logger logger = Logger.getLogger(TimeZoneHelper.class);

    private static final String SENDER_TIMEZONES_FILE = "senderTZs.properties";

    private static final String MATCHER_PATTERN_HOUR_MIN = "(?<hr>\\d{1,2}):(?<min>\\d{2})";
    private static final String MATCHER_PATTERN_TIMEZONE = "(([A-Z]{3,4})|(\\([A-Z]{3,4}\\)))";
    private static final String MATCHER_PATTERN_AM_PM = "[AP][M]";
    private static final String MATCHER_PATTERN_TIME = "\\b" + MATCHER_PATTERN_HOUR_MIN
            + "(\\s*" + MATCHER_PATTERN_AM_PM + ")?"
            + "\\s*" + MATCHER_PATTERN_TIMEZONE + "?\\b*";

    private static final Pattern ampmPattern = Pattern.compile(MATCHER_PATTERN_AM_PM, Pattern.CASE_INSENSITIVE);
    private static final Pattern timeZonePattern = Pattern.compile(MATCHER_PATTERN_TIMEZONE, Pattern.CASE_INSENSITIVE);
    private static final Pattern timePattern = Pattern.compile(MATCHER_PATTERN_TIME, Pattern.CASE_INSENSITIVE);

    private static final ZoneId CENTRAL_EUROPE = ZoneId.of("Europe/Vienna"); // Munich/Brno
    private static final ZoneId EASTERN_NA = ZoneId.of("America/Toronto"); // Toronto
    private static final ZoneId UK = ZoneId.of("Europe/London"); // London
    private static final ZoneId DEFAULT_TZ = EASTERN_NA;

    private static final DateTimeFormatter queryTimeFormat = DateTimeFormatter.ofPattern("H:mZ");
    private static final DateTimeFormatter defaultTimeFormat = DateTimeFormatter.ofPattern("H:m");
    private static final DateTimeFormatter shortTimeZoneFormat = DateTimeFormatter.ofPattern("z");

    private Map<String, ZoneId> senderTZs = getSenderTZs();

    private Map<String, ZoneId> getSenderTZs() {
        Map<String, ZoneId> tzMap = new HashMap<>();
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream(SENDER_TIMEZONES_FILE);
            prop.load(input);
            Enumeration<?> e = prop.propertyNames();
            while (e.hasMoreElements()) {
                String nick = (String) e.nextElement();
                String tzName = prop.getProperty(nick);
                ZoneId tz = ZoneId.of(tzName);
                tzMap.put(nick, tz);
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            logger.warn("File " + SENDER_TIMEZONES_FILE + " not found. Unable to load sender timezones.");
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }
        }
        return tzMap;
    }

    Set<String> getAdjustedTimeMessages(String text, String sender) {
        Set<String> timesMsgs = new HashSet<>();
        Matcher matcher = timePattern.matcher(text);
        while (matcher.find()) {
            String timeString = matcher.group();
            ZonedDateTime queryTime = timeFromString(timeString, sender);
            String localTimes = getLocalTimes(queryTime);
            timesMsgs.add(localTimes);
        }
        return timesMsgs;
    }

    private ZonedDateTime timeFromString(String timeString, String sender) {
        Matcher tzMatcher = timeZonePattern.matcher(timeString);
        ZoneId timezone;
        if (tzMatcher.find()) {
            String tz = tzMatcher.group().toUpperCase();
            if (tz.startsWith("(")) tz = tz.substring(1);
            if (tz.endsWith(")")) tz = tz.substring(0, tz.length() - 1);
            timezone = ZoneId.of(ZoneId.SHORT_IDS.get(tz));
        } else timezone = senderTZs.getOrDefault(sender, DEFAULT_TZ);
        int hrs;
        int idxColon = timeString.indexOf(":");
        try {
            hrs = Integer.parseInt(timeString.substring(idxColon - 2, idxColon));
        } catch (Exception e) {
            hrs = Integer.parseInt(timeString.substring(idxColon - 1, idxColon));
        }
        int mins = Integer.parseInt(timeString.substring(idxColon + 1, idxColon + 3));
        Matcher ampmMatcher = ampmPattern.matcher(timeString);
        if (ampmMatcher.find() && timeString.toUpperCase().contains("PM")) {
            hrs = (hrs + 12) % 24;
        }
        LocalDate localDate = LocalDate.now(timezone);
        LocalTime localTime = LocalTime.of(hrs, mins, 0);
        return ZonedDateTime.of(localDate, localTime, timezone);
    }

    private String getLocalTimes(ZonedDateTime queryTime) {
        StringBuilder sb = new StringBuilder();
        sb.append(queryTime.format(queryTimeFormat) + " -- ");
        ZonedDateTime CET = queryTime.withZoneSameInstant(CENTRAL_EUROPE);
        sb.append(String.format("Munich/Brno (%s): %s | ", CET.format(shortTimeZoneFormat), CET.format(defaultTimeFormat)));
        ZonedDateTime EST = queryTime.withZoneSameInstant(EASTERN_NA);
        sb.append(String.format("Toronto (%s): %s | ", EST.format(shortTimeZoneFormat), EST.format(defaultTimeFormat)));
        ZonedDateTime GMT = queryTime.withZoneSameInstant(UK);
        sb.append(String.format("London (%s): %s", GMT.format(shortTimeZoneFormat), GMT.format(defaultTimeFormat)));
        return sb.toString();
    }
}