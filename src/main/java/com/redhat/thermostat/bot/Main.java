package com.redhat.thermostat.bot;

import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.log4j.Logger;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.Arrays;

public class Main extends ListenerAdapter{

    static Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(
                PropertiesConfiguration.class).configure(params.properties().setFileName("bot.properties"));
        try {
            org.apache.commons.configuration2.Configuration propertiesConfig = builder.getConfiguration();
            String nick = propertiesConfig.getString("nick");
            String username = propertiesConfig.getString("username");
            String password = propertiesConfig.getString("password");
            String server = propertiesConfig.getString("server");
            String[] channels = propertiesConfig.getStringArray("channels");
            logger.info("Successfully retrieved bot properties from config file.");

            org.pircbotx.Configuration botConfiguration = new org.pircbotx.Configuration.Builder()
                    .setName(nick)
                    .setLogin(username)
                    .setServerPassword(password)
                    .addServer(server)
                    .addAutoJoinChannels(Arrays.asList(channels))
                    .setAutoReconnect(true)
                    .buildConfiguration();

            PircBotX bot = new PircBotX(botConfiguration);
            logger.info("Starting bot...");
            bot.startBot();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void onGenericMessage(GenericMessageEvent event) {
        if (event.getMessage().startsWith("?helloworld"))
            logger.info("Responding to ?helloworld");
            event.respond("Hello world!");
    }
}
