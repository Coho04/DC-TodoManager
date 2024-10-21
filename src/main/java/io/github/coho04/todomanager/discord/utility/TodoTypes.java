package io.github.coho04.todomanager.discord.utility;

import io.github.coho04.todomanager.Main;
import io.github.coho04.todomanager.MysqlConnection;
import io.github.coho04.mysql.entities.SearchResult;
import io.github.coho04.mysql.entities.Table;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.Color;
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
        Table table = Main.getMysqlConnection().getMysql().getDatabase(Main.getCustomConfig().getMysqlDatabase()).getTable(MysqlConnection.settingTable);
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
