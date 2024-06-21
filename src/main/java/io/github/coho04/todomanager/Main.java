package io.github.coho04.todomanager;

import io.github.coho04.todomanager.discord.Events;
import io.github.coho04.todomanager.discord.commands.Settings;
import io.github.coho04.todomanager.discord.commands.Todo;
import io.github.coho04.dcbcore.DCBot;
import io.github.coho04.dcbcore.DCBotBuilder;

public class Main {

    private static MysqlConnection mysqlConnection;
    private static DCBot dcBot;

    public static void main(String[] args) {
        CustomConfig config = new CustomConfig();
        mysqlConnection = new MysqlConnection(config.getMysqlHostname(), config.getMysqlUsername(), config.getMysqlPassword(), config.getMysqlPort());

        DCBotBuilder dcBotBuilder = new DCBotBuilder(args, true);
        dcBotBuilder.registerCommands(new Todo(), new Settings());
        dcBotBuilder.registerEvents(new Events());
        dcBot = dcBotBuilder.build();
    }

    public static MysqlConnection getMysqlConnection() {
        return mysqlConnection;
    }

    public static DCBot getDcBot() {
        return dcBot;
    }
}