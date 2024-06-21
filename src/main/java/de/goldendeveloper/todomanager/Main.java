package de.goldendeveloper.todomanager;

import de.goldendeveloper.todomanager.discord.Events;
import de.goldendeveloper.todomanager.discord.commands.Settings;
import de.goldendeveloper.todomanager.discord.commands.Todo;
import io.github.coho04.dcbcore.DCBot;
import io.github.coho04.dcbcore.DCBotBuilder;

import java.sql.SQLException;

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