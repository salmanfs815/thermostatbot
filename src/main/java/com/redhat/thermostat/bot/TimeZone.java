package com.redhat.thermostat.bot;

import org.apache.log4j.Logger;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.time.Clock;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeZone extends ListenerAdapter {

    private static Logger logger = Logger.getLogger(TimeZone.class);

    private static final Pattern BASE_NICK_PATTERN = Pattern.compile("([a-zA-Z0-9]+).*");

    private TimeZoneHelper tzHelper;

    TimeZone() {
        tzHelper = new TimeZoneHelper();
    }

    TimeZone(Clock clock) {
        tzHelper = new TimeZoneHelper(clock);
    }

    @Override
    public void onGenericMessage(final GenericMessageEvent event) {
        String text = event.getMessage();
        if (event instanceof PrivateMessageEvent || text.toLowerCase().contains(event.getBot().getNick().toLowerCase())) {
            String sender = getBaseNick(event.getUser().getNick());
            Set<String> timeMessages = tzHelper.getAdjustedTimeMessages(text, sender);
            for (String timeMessage : timeMessages) {
                logger.info(String.format("[%s] %s", event.getBot().getNick(), timeMessage));
                event.respondWith(timeMessage);
            }
        }
    }

    private static String getBaseNick(String currentNick) {
        Matcher nickMatcher = BASE_NICK_PATTERN.matcher(currentNick);
        if (nickMatcher.matches()) {
            return nickMatcher.group(1);
        } else {
            return currentNick;
        }
    }
}

