package io.github.coho04.todomanager.discord;

import io.github.coho04.todomanager.discord.utility.TodoList;
import io.github.coho04.todomanager.discord.utility.TodoTypes;
import io.github.coho04.todomanager.Main;
import io.github.coho04.todomanager.MysqlConnection;
import io.github.coho04.mysql.entities.RowBuilder;
import io.github.coho04.mysql.entities.Table;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;
import java.time.Instant;
import java.util.Date;

public class Events extends ListenerAdapter {

    @Override
    public void onModalInteraction(ModalInteractionEvent e) {
        if (e.getModalId().equals(TodoList.addTodoListModelID)) {
            String title = e.getValue(TodoList.addTodoListModelTitle).getAsString();
            String description = e.getValue(TodoList.addTodoListModelDescription).getAsString();
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle(title);
            embedBuilder.setDescription(description);
            embedBuilder.addField("Zuletzt aktualisiert", "Von: " + e.getUser().getAsMention(), false);
            embedBuilder.setTimestamp(new Date().toInstant());
            embedBuilder.setFooter("@TodoManager", e.getJDA().getSelfUser().getAvatarUrl());
            embedBuilder.setColor(Color.GREEN);
            embedBuilder.addField("Todo-ID", "#" + Instant.now().getEpochSecond(), false);
            e.reply("Das Todo wurde hinzugefügt!").setEphemeral(true).queue();
            TodoTypes.OPEN.getChannel(e.getGuild()).sendMessageEmbeds(embedBuilder.build()).queue();
        }
    }

    @Override
    public void onGuildJoin(GuildJoinEvent e) {
        Table table = Main.getMysqlConnection().getMysql().getDatabase(MysqlConnection.dbName).getTable(MysqlConnection.settingTable);
        if (!table.getColumn(MysqlConnection.clmGuildID).getAll().getAsString().contains(e.getGuild().getId())) {
            e.getGuild().createRole().queue(role -> {
                role.getManager().setName("Todo").queue();
                table.insert(
                        new RowBuilder()
                                .with(table.getColumn(MysqlConnection.clmGuildID), e.getGuild().getId())
                                .with(table.getColumn(TodoTypes.OPEN.getColumnName()), "")
                                .with(table.getColumn(TodoTypes.WAITING.getColumnName()), "")
                                .with(table.getColumn(TodoTypes.CLOSED.getColumnName()), "")
                                .with(table.getColumn(MysqlConnection.clmPermRole), role.getId())
                                .build()
                );
            });
        }
    }
}
