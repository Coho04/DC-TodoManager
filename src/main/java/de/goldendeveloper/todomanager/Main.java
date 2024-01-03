package de.goldendeveloper.todomanager;

import de.goldendeveloper.dcbcore.DCBot;
import de.goldendeveloper.dcbcore.DCBotBuilder;
import de.goldendeveloper.mysql.exceptions.NoConnectionException;
import de.goldendeveloper.todomanager.discord.Events;
import de.goldendeveloper.todomanager.discord.commands.Settings;
import de.goldendeveloper.todomanager.discord.commands.Todo;

import java.sql.SQLException;

public class Main {

    private static MysqlConnection mysqlConnection;
    private static DCBot dcBot;

    public static void main(String[] args) throws NoConnectionException, SQLException {
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