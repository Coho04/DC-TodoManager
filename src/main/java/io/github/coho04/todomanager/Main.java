package io.github.coho04.todomanager;

import io.github.coho04.todomanager.discord.Events;
import io.github.coho04.todomanager.discord.commands.Settings;
import io.github.coho04.todomanager.discord.commands.Todo;
import io.github.coho04.dcbcore.DCBot;
import io.github.coho04.dcbcore.DCBotBuilder;

public class Main {

    private static MysqlConnection mysqlConnection;
    private static CustomConfig customConfig;
    private static DCBot dcBot;

    public static void main(String[] args) {
        customConfig = new CustomConfig();
        mysqlConnection = new MysqlConnection(customConfig.getMysqlHostname(), customConfig.getMysqlUsername(), customConfig.getMysqlPassword(), customConfig.getMysqlPort());

        DCBotBuilder dcBotBuilder = new DCBotBuilder(args, true);
        dcBotBuilder.registerCommands(new Todo(), new Settings());
        dcBotBuilder.registerEvents(new Events());
        dcBot = dcBotBuilder.build();
        System.out.println("Java application started successfully");
    }

    public static MysqlConnection getMysqlConnection() {
        return mysqlConnection;
    }

    public static CustomConfig getCustomConfig() {
        return customConfig;
    }

    public static DCBot getDcBot() {
        return dcBot;
    }
}