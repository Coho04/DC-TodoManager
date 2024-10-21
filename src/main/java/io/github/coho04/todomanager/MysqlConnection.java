package io.github.coho04.todomanager;

import io.github.coho04.todomanager.discord.utility.TodoTypes;
import io.github.coho04.mysql.MYSQL;
import io.github.coho04.mysql.entities.Database;
import io.github.coho04.mysql.entities.Table;

public class MysqlConnection {

    private final MYSQL mysql;
    public static String settingTable = "settings";
    public static String clmGuildID = "guild";
    public static String clmPermRole = "role";

    public MysqlConnection(String hostname, String username, String password, int port) {
        mysql = new MYSQL(hostname, username, password, port);
        if (!mysql.existsDatabase(Main.getCustomConfig().getMysqlDatabase())) {
            mysql.createDatabase(Main.getCustomConfig().getMysqlDatabase());
        }
        Database db = mysql.getDatabase(Main.getCustomConfig().getMysqlDatabase());
        if (!db.existsTable(settingTable)) {
            db.createTable(settingTable);
        }
        Table table = db.getTable(settingTable);
        if (!table.existsColumn(clmGuildID)) {
            table.addColumn(clmGuildID);
        }
        if (!table.existsColumn(clmPermRole)) {
            table.addColumn(clmPermRole);
        }
        TodoTypes.getAllTodoTypes().forEach(todoType -> {
            if (!table.existsColumn(todoType.getColumnName())) {
                table.addColumn(todoType.getColumnName());
            }
        });
        System.out.println("MYSQL Finished");
    }

    public MYSQL getMysql() {
        return mysql;
    }
}
