package com.redhat.thermostat.bot;

import org.junit.Before;
import org.junit.Test;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TimeZoneTest {

    private static String botNick = "thermostatbot";

    private MessageEvent msgEvent;
    private PrivateMessageEvent privMsgEvent;
    private TimeZone timeZone;

    @Before
    public void setupTest() {
        String channel = "#jmc";
        String userNick = "sasiddiq";
        String userTimeZone = "America/Toronto";

        PircBotX bot = mock(PircBotX.class);
        when(bot.getNick()).thenReturn(botNick);

        Channel chan = mock(Channel.class);
        when(chan.getName()).thenReturn(channel);

        User user = mock(User.class);
        when(user.getNick()).thenReturn(userNick);

        msgEvent = mock(MessageEvent.class);
        when(msgEvent.getBot()).thenReturn(bot);
        when(msgEvent.getChannel()).thenReturn(chan);
        when(msgEvent.getUser()).thenReturn(user);

        privMsgEvent = mock(PrivateMessageEvent.class);
        when(privMsgEvent.getBot()).thenReturn(bot);
        when(privMsgEvent.getUser()).thenReturn(user);

        TimeZoneHelper.senderTZs = new HashMap<>();
        TimeZoneHelper.senderTZs.put(userNick, ZoneId.of(userTimeZone));

        timeZone = new TimeZone();
    }

    @Test
    public void timezoneChannelMessageTest() {
        String expectedReply = "10:15-0500 -- Munich/Brno (CET): 16:15 | Toronto (EST): 10:15 | London (GMT): 15:15";
        Set<String> timeQueries = new HashSet<>();
        timeQueries.add("10:15");
        timeQueries.add("10:15 am");
        timeQueries.add("10:15 AM");
        timeQueries.add("10:15           am");
        timeQueries.add("         10:15");
        timeQueries.add("10:15 AM        ");
        timeQueries.add("10:15 am est");
        timeQueries.add("10:15 AM EST");
        timeQueries.add("10:15 AM (EST)");
        timeQueries.add("10:15 am (est)");
        timeQueries.add("10:15    am        est");
        timeQueries.add("10:15      am  (est)  ");
        timeQueries.add("blah blah 10:15 am (est) blah blah blah");
        for (String query: timeQueries) {
            when(msgEvent.getMessage()).thenReturn(botNick + ", " + query);
            timeZone.onGenericMessage(msgEvent);
        }
        verify(msgEvent, times(timeQueries.size())).respondWith(expectedReply);
    }

    @Test
    public void timezonePrivateMessageTest() {
        String expectedReply = "10:15-0500 -- Munich/Brno (CET): 16:15 | Toronto (EST): 10:15 | London (GMT): 15:15";
        Set<String> timeQueries = new HashSet<>();
        timeQueries.add("10:15");
        timeQueries.add("10:15 am");
        timeQueries.add("10:15 AM");
        timeQueries.add("10:15           am");
        timeQueries.add("         10:15");
        timeQueries.add("10:15 AM        ");
        timeQueries.add("10:15 am est");
        timeQueries.add("10:15 AM EST");
        timeQueries.add("10:15 AM (EST)");
        timeQueries.add("10:15 am (est)");
        timeQueries.add("10:15    am        est");
        timeQueries.add("10:15      am  (est)  ");
        timeQueries.add("blah blah 10:15 am (est) blah blah blah");
        for (String query: timeQueries) {
            when(privMsgEvent.getMessage()).thenReturn(query);
            timeZone.onGenericMessage(privMsgEvent);
        }
        verify(privMsgEvent, times(timeQueries.size())).respondWith(expectedReply);
    }

    @Test
    public void timezoneMultipleQueriesTest() {
        String ircmsg = botNick + ", " + "blah blah 10:15 am blah 15:34 (PST) blah blah blah 3:35 PM JST";
        String expectedReply1 = "15:34-0800 -- Munich/Brno (CET): 0:34 | Toronto (EST): 18:34 | London (GMT): 23:34";
        String expectedReply2 = "15:35+0900 -- Munich/Brno (CET): 7:35 | Toronto (EST): 1:35 | London (GMT): 6:35";
        String expectedReply3 = "10:15-0500 -- Munich/Brno (CET): 16:15 | Toronto (EST): 10:15 | London (GMT): 15:15";
        when(msgEvent.getMessage()).thenReturn(ircmsg);
        timeZone.onGenericMessage(msgEvent);
        verify(msgEvent, times(1)).respondWith(expectedReply1);
        verify(msgEvent, times(1)).respondWith(expectedReply2);
        verify(msgEvent, times(1)).respondWith(expectedReply3);
    }

    @Test
    public void defaultToEasternTimeTest() {
        User user = mock(User.class);
        when(msgEvent.getUser()).thenReturn(user);
        when(user.getNick()).thenReturn("some_random_guy");

        String ircmsg = botNick + ", " + "10:15";
        String expectedReply = "10:15-0500 -- Munich/Brno (CET): 16:15 | Toronto (EST): 10:15 | London (GMT): 15:15";
        when(msgEvent.getMessage()).thenReturn(ircmsg);
        timeZone.onGenericMessage(msgEvent);
        verify(msgEvent, times(1)).respondWith(expectedReply);
    }
}
