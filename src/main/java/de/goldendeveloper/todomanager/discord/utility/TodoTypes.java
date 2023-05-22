package de.goldendeveloper.todomanager.discord.utility;

import de.goldendeveloper.mysql.entities.SearchResult;
import de.goldendeveloper.mysql.entities.Table;
import de.goldendeveloper.todomanager.Main;
import de.goldendeveloper.todomanager.MysqlConnection;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.util.HashMap;
import java.util.List;

public enum TodoTypes {
    OPEN("Offen", "open", Color.GREEN),
    WAITING("Wartend", "process", Color.CYAN),
    CLOSED("Geschlossen", "closed", Color.RED);

    private final String name;
    private final String value;
    private final String columnName;
    private final Color color;

    TodoTypes(String name, String columnName, Color color) {
        this.name = name;
        this.value = name().toLowerCase();
        this.columnName = columnName;
        this.color = color;
    }

    public static List<TodoTypes> getAllTodoTypes() {
        return List.of(TodoTypes.values());
    }

    public String getColumnName() {
        return columnName;
    }

    public Color getColor() {
        return color;
    }

    public TextChannel getChannel(Guild guild) {
        Table table = Main.getMysqlConnection().getMysql().getDatabase(MysqlConnection.dbName).getTable(MysqlConnection.settingTable);
        HashMap<String, SearchResult> row = table.getRow(table.getColumn(MysqlConnection.clmGuildID), guild.getId()).getData();
        return Main.getDcBot().getDiscord().getBot().getTextChannelById(row.get(columnName).toString());
    }

    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }
}
