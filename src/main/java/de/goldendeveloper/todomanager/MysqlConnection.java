package de.goldendeveloper.todomanager;

import de.goldendeveloper.mysql.MYSQL;
import de.goldendeveloper.mysql.entities.Database;
import de.goldendeveloper.mysql.entities.MysqlTypes;
import de.goldendeveloper.mysql.entities.Table;

public class MysqlConnection {

    private final MYSQL mysql;
    public static String dbName = "GD-TodoManager";
    public static String settingTable = "settings";
    public static String clmGuildID = "guild";
    public static String clmOpenChannel = "open";
    public static String clmProcessChannel = "process";
    public static String clmClosedChannel = "closed";

    public MysqlConnection(String hostname, String username, String password, int port) {
        mysql = new MYSQL(hostname, username, password, port);
        if (!mysql.existsDatabase(dbName)) {
            mysql.createDatabase(dbName);
        }
        Database db = mysql.getDatabase(dbName);
        if (!db.existsTable(settingTable)) {
            db.createTable(settingTable);
        }
        Table table = db.getTable(settingTable);
        table.addColumn(clmGuildID, MysqlTypes.VARCHAR, 80);
        table.addColumn(clmOpenChannel, MysqlTypes.VARCHAR, 80);
        table.addColumn(clmProcessChannel, MysqlTypes.VARCHAR, 80);
        table.addColumn(clmClosedChannel, MysqlTypes.VARCHAR, 80);
    }

    public MYSQL getMysql() {
        return mysql;
    }
}
