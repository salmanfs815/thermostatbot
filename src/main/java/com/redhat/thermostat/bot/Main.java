package com.redhat.thermostat.bot;

import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.log4j.Logger;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.io.IOException;
import java.util.Arrays;

public class Main extends ListenerAdapter{

    private static Logger logger = Logger.getLogger(Main.class);

    private static final String BOT_CONFIG_FILE = "bot.properties";

    private static org.apache.commons.configuration2.Configuration propertiesConfig = getPropertiesConfig();

    public static void main(String[] args) {
        String nick = propertiesConfig.getString("nick");
        String username = propertiesConfig.getString("username");
        String password = propertiesConfig.getString("password");
        String server = propertiesConfig.getString("server");
        String[] channels = propertiesConfig.getStringArray("channels");
        logger.info("Successfully retrieved bot properties from config file.");
        org.pircbotx.Configuration botConfiguration = new org.pircbotx.Configuration.Builder()
                .setName(nick)
                .setRealName(nick + " IRC Bot")
                .setLogin(username)
                .setNickservPassword(password)
                .addServer(server)
                .addAutoJoinChannels(Arrays.asList(channels))
                .setAutoReconnect(true)
                .addListener(new Main())
                .addListener(new BugInfo())
                .addListener(new TimeZone())
                .buildConfiguration();

        PircBotX bot = new PircBotX(botConfiguration);
        logger.info("Starting bot...");
        try {
            bot.startBot();
        } catch (IOException | IrcException e) {
            logger.error(e.getMessage());
            System.exit(1);
        }
    }

    private static org.apache.commons.configuration2.Configuration getPropertiesConfig() {
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                new FileBasedConfigurationBuilder<FileBasedConfiguration>(
                        PropertiesConfiguration.class).configure(params.properties()
                        .setFileName(BOT_CONFIG_FILE).setListDelimiterHandler(new DefaultListDelimiterHandler(' ')));
        org.apache.commons.configuration2.Configuration propertiesConfig = null;
        try {
            propertiesConfig = builder.getConfiguration();
        } catch (ConfigurationException e) {
            logger.error(e.getMessage());
            System.exit(1);
        }
        return propertiesConfig;
    }

    public void onConnect(ConnectEvent event) {
        logger.info("Connected to IRC server");
    }

    @Override
    public void onGenericMessage(final GenericMessageEvent event) {
        logger.info(String.format("[%s] %s", event.getUser().getNick(), event.getMessage()));

        String msg = event.getMessage().toLowerCase();
        String botNick = event.getBot().getNick().toLowerCase();
        if (msg.contains(botNick) && msg.contains("help")) {
            displayHelp(event);
        }
    }

    private void displayHelp(GenericMessageEvent event) {
        String helpMsg =
                "So as to not disturb your conversations, I will only speak if I am mentioned in your message.\n" +
                "I know about IcedTea, Red Hat, JDK and JMC bugs, and times/timezones.\n" +
                "To get the description for an IcedTea bug, type \"PR<bugid>\" in the channel.\n" +
                "To get the description for a Red Hat bug, type \"RH<bugid>\" or \"RHBZ<bugid>\" in the channel\n" +
                "To get the description for an JDK bug, type \"JDK-<bugid>\" in the channel.\n" +
                "To get the description for an JMC bug, type \"JMC-<bugid>\" in the channel.\n" +
                "These are not case sensitive.\n" +
                "If you type a time of day in the channel, I will repeat it in several relevant time zones.\n" +
                "The time format that I recognize is: \"HH:mm[am/pm] (ZONE)\".\n" +
                "If you do not specify am or pm, I will assume a 24-hour clock.\n" +
                "ZONE should be the abbreviation for your current time zone, such as EDT or CEST.\n" +
                "If you do not include a zone, I will make a guess based on what time zone I think you are in.\n";
        for (String line: helpMsg.split("\n")) {
            event.respondPrivateMessage(line);
        }
    }
}
