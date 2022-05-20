package de.goldendeveloper.todomanager;

import de.goldendeveloper.todomanager.discord.Discord;

public class Main {

    private static Discord discord;
    private  static Config config;
    private  static MysqlConnection mysqlConnection;

    public static void main(String[] args)  {
        config = new Config();
        mysqlConnection = new MysqlConnection(config.getMysqlHostname(), config.getMysqlUsername(), config.getMysqlPassword(), config.getMysqlPort());
        discord = new Discord(config.getDiscordToken());
    }

    public static Config getConfig() {
        return config;
    }

    public static MysqlConnection getMysqlConnection() {
        return mysqlConnection;
    }

    public static Discord getDiscord() {
        return discord;
    }
}