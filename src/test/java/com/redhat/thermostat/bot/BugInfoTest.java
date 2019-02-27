package com.redhat.thermostat.bot;

import org.junit.Before;
import org.junit.Test;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BugInfoTest {

    private static String botNick = "thermostatbot";

    private MessageEvent msgEvent;
    private PrivateMessageEvent privMsgEvent;
    private BugInfo bugInfo;

    @Before
    public void setupTest() {
        PircBotX bot = mock(PircBotX.class);
        User user = mock(User.class);
        Channel chan = mock(Channel.class);
        msgEvent = mock(MessageEvent.class);
        privMsgEvent = mock(PrivateMessageEvent.class);
        when(msgEvent.getBot()).thenReturn(bot);
        when(privMsgEvent.getBot()).thenReturn(bot);
        when(bot.getNick()).thenReturn(botNick);
        when(msgEvent.getUser()).thenReturn(user);
        when(privMsgEvent.getUser()).thenReturn(user);
        String userNick = "sasiddiq";
        when(user.getNick()).thenReturn(userNick);
        when(msgEvent.getChannel()).thenReturn(chan);
        String channel = "#jmc";
        when(chan.getName()).thenReturn(channel);
        bugInfo = new BugInfo();
    }

    @Test
    public void jmcNormalBugTest() {
        String ircmsg = botNick + ", " + "jmc5385";
        String expectedReply = "[Java Mission Control][Mission Control Client] JMC-5385: Alert dialog not always appearing when something triggered (https://bugs.openjdk.java.net/browse/JMC-5385)";
        when(msgEvent.getMessage()).thenReturn(ircmsg);
        bugInfo.onGenericMessage(msgEvent);
        verify(msgEvent, times(1)).respondWith(expectedReply);
    }

    @Test
    public void jmcLoginRequiredBugTest() {
        String ircmsg = botNick + ", " + "  JMC-1234";
        String expectedReply = "JMC-1234: You do not have the permission to see the specified issue. Login Required";
        when(msgEvent.getMessage()).thenReturn(ircmsg);
        bugInfo.onGenericMessage(msgEvent);
        verify(msgEvent, times(1)).respondWith(expectedReply);
    }

    @Test
    public void jmcDNEBugTest() {
        String ircmsg = botNick + ", " + "JMC 999999  ";
        String expectedReply = "JMC-999999: Issue Does Not Exist";
        when(msgEvent.getMessage()).thenReturn(ircmsg);
        bugInfo.onGenericMessage(msgEvent);
        verify(msgEvent, times(1)).respondWith(expectedReply);
    }

    @Test
    public void jmcNormalBugPrivMsgTest() {
        String ircmsg = "jmc5385  ";
        String expectedReply = "[Java Mission Control][Mission Control Client] JMC-5385: Alert dialog not always appearing when something triggered (https://bugs.openjdk.java.net/browse/JMC-5385)";
        when(privMsgEvent.getMessage()).thenReturn(ircmsg);
        bugInfo.onGenericMessage(privMsgEvent);
        verify(privMsgEvent, times(1)).respondWith(expectedReply);
    }

    @Test
    public void jmcLoginRequiredBugPrivMsgTest() {
        String ircmsg = "  JMC-1234";
        String expectedReply = "JMC-1234: You do not have the permission to see the specified issue. Login Required";
        when(privMsgEvent.getMessage()).thenReturn(ircmsg);
        bugInfo.onGenericMessage(privMsgEvent);
        verify(privMsgEvent, times(1)).respondWith(expectedReply);
    }

    @Test
    public void jmcDNEBugPrivMsgTest() {
        String ircmsg = "JMC 999999";
        String expectedReply = "JMC-999999: Issue Does Not Exist";
        when(privMsgEvent.getMessage()).thenReturn(ircmsg);
        bugInfo.onGenericMessage(privMsgEvent);
        verify(privMsgEvent, times(1)).respondWith(expectedReply);
    }

    @Test
    public void jmcNoBotNickBugTest() {
        String ircmsg = "jmc5385";
        when(msgEvent.getMessage()).thenReturn(ircmsg);
        bugInfo.onGenericMessage(msgEvent);
        verify(msgEvent, times(0)).respondWith("");
    }

    @Test
    public void jmcInvalidFormatBugTest() {
        String ircmsg = botNick + ", " + "jmc  5385";
        when(msgEvent.getMessage()).thenReturn(ircmsg);
        bugInfo.onGenericMessage(msgEvent);
        verify(msgEvent, times(0)).respondWith("");
    }

    @Test
    public void jmcNoBugTest() {
        String ircmsg = botNick + ", " + "openjdk11";
        when(msgEvent.getMessage()).thenReturn(ircmsg);
        bugInfo.onGenericMessage(msgEvent);
        verify(msgEvent, times(0)).respondWith("");
    }

    @Test
    public void jdkNormalBugTest() {
        String ircmsg = botNick + ", " + "jdk8219772";
        String expectedReply = "[JDK][infrastructure] JDK-8219772: EXTRA_CFLAGS not being picked up for assembler files (https://bugs.openjdk.java.net/browse/JDK-8219772)";
        when(msgEvent.getMessage()).thenReturn(ircmsg);
        bugInfo.onGenericMessage(msgEvent);
        verify(msgEvent, times(1)).respondWith(expectedReply);
    }

    @Test
    public void jdkDNEBugTest() {
        String ircmsg = botNick + ", " + "JDK 1024686  ";
        String expectedReply = "JDK-1024686: Issue Does Not Exist";
        when(msgEvent.getMessage()).thenReturn(ircmsg);
        bugInfo.onGenericMessage(msgEvent);
        verify(msgEvent, times(1)).respondWith(expectedReply);
    }

    @Test
    public void jdkNormalBugPrivMsgTest() {
        String ircmsg = "jdk8219772  ";
        String expectedReply = "[JDK][infrastructure] JDK-8219772: EXTRA_CFLAGS not being picked up for assembler files (https://bugs.openjdk.java.net/browse/JDK-8219772)";
        when(privMsgEvent.getMessage()).thenReturn(ircmsg);
        bugInfo.onGenericMessage(privMsgEvent);
        verify(privMsgEvent, times(1)).respondWith(expectedReply);
    }

    @Test
    public void jdkDNEBugPrivMsgTest() {
        String ircmsg = "JDK 1024686  ";
        String expectedReply = "JDK-1024686: Issue Does Not Exist";
        when(privMsgEvent.getMessage()).thenReturn(ircmsg);
        bugInfo.onGenericMessage(privMsgEvent);
        verify(privMsgEvent, times(1)).respondWith(expectedReply);
    }

    @Test
    public void jdkNoBotNickBugTest() {
        String ircmsg = "jdk8219772";
        when(msgEvent.getMessage()).thenReturn(ircmsg);
        bugInfo.onGenericMessage(msgEvent);
        verify(msgEvent, times(0)).respondWith("");
    }

    @Test
    public void jdkInvalidFormatBugTest() {
        String ircmsg = botNick + ", " + "jdk    8219772";
        when(msgEvent.getMessage()).thenReturn(ircmsg);
        bugInfo.onGenericMessage(msgEvent);
        verify(msgEvent, times(0)).respondWith("");
    }

    @Test
    public void jdkNoBugTest() {
        String ircmsg = botNick + ", " + "OracleJDK 11";
        when(msgEvent.getMessage()).thenReturn(ircmsg);
        bugInfo.onGenericMessage(msgEvent);
        verify(msgEvent, times(0)).respondWith("");
    }

    @Test
    public void rhNormalBugTest() {
        String expectedReply = "[Red Hat Linux][installer] RH-1234: Swap partition & Installer (https://bugzilla.redhat.com/show_bug.cgi?id=1234)";
        Set<String> bugQueries = new HashSet<>();
        bugQueries.add("  RH 1234");
        bugQueries.add("rhbz 1234  ");
        bugQueries.add(" rh-1234 ");
        bugQueries.add("   rhbz-1234    ");
        bugQueries.add("RHBZ-1234");
        for (String query: bugQueries) {
            when(msgEvent.getMessage()).thenReturn(botNick + ", " + query);
            bugInfo.onGenericMessage(msgEvent);
        }
        verify(msgEvent, times(bugQueries.size())).respondWith(expectedReply);
    }

    @Test
    public void rhNormalBugPrivMsgTest() {
        String expectedReply = "[Red Hat Linux][installer] RH-1234: Swap partition & Installer (https://bugzilla.redhat.com/show_bug.cgi?id=1234)";

        Set<String> bugQueries = new HashSet<>();
        bugQueries.add("  RH 1234");
        bugQueries.add("rhbz 1234  ");
        bugQueries.add(" rh-1234 ");
        bugQueries.add("   rhbz-1234    ");
        bugQueries.add("RHBZ-1234");
        for (String query: bugQueries) {
            when(privMsgEvent.getMessage()).thenReturn(query);
            bugInfo.onGenericMessage(privMsgEvent);
        }
        verify(privMsgEvent, times(bugQueries.size())).respondWith(expectedReply);
    }

    @Test
    public void rhDNEBugTest() {
        String ircmsg = botNick + ", " + "rhbz-99999";
        String expectedReply = "Red Hat bug #99999 not found";
        when(msgEvent.getMessage()).thenReturn(ircmsg);
        bugInfo.onGenericMessage(msgEvent);
        verify(msgEvent, times(1)).respondWith(expectedReply);
    }

    @Test
    public void rhDNEBugPrivMsgTest() {
        String ircmsg = "rhbz-99999";
        String expectedReply = "Red Hat bug #99999 not found";
        when(privMsgEvent.getMessage()).thenReturn(ircmsg);
        bugInfo.onGenericMessage(privMsgEvent);
        verify(privMsgEvent, times(1)).respondWith(expectedReply);
    }

    @Test
    public void rhNoBotNickBugTest() {
        String ircmsg = "rh 1234";
        when(msgEvent.getMessage()).thenReturn(ircmsg);
        bugInfo.onGenericMessage(msgEvent);
        verify(msgEvent, times(0)).respondWith("");
    }

    @Test
    public void rhInvalidFormatBugTest() {
        String ircmsg = botNick + ", " + "rhbz      1234";
        when(msgEvent.getMessage()).thenReturn(ircmsg);
        bugInfo.onGenericMessage(msgEvent);
        verify(msgEvent, times(0)).respondWith("");
    }

    @Test
    public void icedteaNormalBugTest() {
        String expectedReply = "[IcedTea][IcedTea] PR-123: Fail to build with static libstdc++ (https://icedtea.classpath.org/bugzilla/show_bug.cgi?id=123)";

        Set<String> bugQueries = new HashSet<>();
        bugQueries.add("  PR 123");
        bugQueries.add("pr 123  ");
        bugQueries.add(" pr         123  ");
        bugQueries.add("   Pr123    ");
        bugQueries.add("pR123");
        for (String query: bugQueries) {
            when(msgEvent.getMessage()).thenReturn(botNick + ", " + query);
            bugInfo.onGenericMessage(msgEvent);
        }
        verify(msgEvent, times(bugQueries.size())).respondWith(expectedReply);
    }

    @Test
    public void icedteaNormalBugPrivMsgTest() {
        String expectedReply = "[IcedTea][IcedTea] PR-123: Fail to build with static libstdc++ (https://icedtea.classpath.org/bugzilla/show_bug.cgi?id=123)";

        Set<String> bugQueries = new HashSet<>();
        bugQueries.add("  PR 123");
        bugQueries.add("pr 123  ");
        bugQueries.add(" pr         123  ");
        bugQueries.add("   Pr123    ");
        bugQueries.add("pR123");
        for (String query: bugQueries) {
            when(privMsgEvent.getMessage()).thenReturn(botNick + ", " + query);
            bugInfo.onGenericMessage(privMsgEvent);
        }
        verify(privMsgEvent, times(bugQueries.size())).respondWith(expectedReply);
    }

    @Test
    public void icedteaDNEBugTest() {
        String ircmsg = botNick + ", " + "pr 987654";
        String expectedReply = "Iced Tea bug #987654 does not exist";
        when(msgEvent.getMessage()).thenReturn(ircmsg);
        bugInfo.onGenericMessage(msgEvent);
        verify(msgEvent, times(1)).respondWith(expectedReply);
    }

    @Test
    public void icedteaDNEBugPrivMsgTest() {
        String ircmsg = "pr 987654";
        String expectedReply = "Iced Tea bug #987654 does not exist";
        when(privMsgEvent.getMessage()).thenReturn(ircmsg);
        bugInfo.onGenericMessage(privMsgEvent);
        verify(privMsgEvent, times(1)).respondWith(expectedReply);
    }

    @Test
    public void icedteaInvalidQueriesTest() {
        Set<String> bugQueries = new HashSet<>();
        bugQueries.add("PRa101");
        bugQueries.add("APR101");
        bugQueries.add("PR101ABZ");
        for (String query: bugQueries) {
            when(msgEvent.getMessage()).thenReturn(botNick + ", " + query);
            bugInfo.onGenericMessage(msgEvent);
        }
        verify(msgEvent, times(0)).respondWith("");
    }
}
