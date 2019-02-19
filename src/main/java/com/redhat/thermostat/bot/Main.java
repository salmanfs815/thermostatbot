package com.redhat.thermostat.bot;

import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.log4j.Logger;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.types.GenericEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.io.IOException;
import java.util.Arrays;

public class Main extends ListenerAdapter{

    static Logger logger = Logger.getLogger(Main.class);
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
                .buildConfiguration();

        PircBotX bot = new PircBotX(botConfiguration);
        logger.info("Starting bot...");
        try {
            bot.startBot();
        } catch (IOException | IrcException e) {
            logger.error(e.getMessage());
            System.exit(1);
        }
        if (bot.isConnected()) {
            logger.info("Connected to server!");
        } else {
            logger.info("Not yet connected to server.");
        }
    }

    private static org.apache.commons.configuration2.Configuration getPropertiesConfig() {
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(
                PropertiesConfiguration.class).configure(params.properties().setFileName("bot.properties"));
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
        logger.debug("msg received: " + event.getMessage());
        if (event.getMessage().startsWith("?helloworld")) {
            logger.info("Responding to ?helloworld");
        }
        event.respond("Hello world!");
    }
}
