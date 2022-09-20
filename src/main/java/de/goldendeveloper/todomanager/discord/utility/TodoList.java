package de.goldendeveloper.todomanager.discord.utility;

import de.goldendeveloper.mysql.entities.SearchResult;
import de.goldendeveloper.mysql.entities.Table;
import de.goldendeveloper.todomanager.Main;
import de.goldendeveloper.todomanager.MysqlConnection;
import de.goldendeveloper.todomanager.discord.Discord;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import java.awt.*;
import java.util.Date;
import java.util.HashMap;

public class TodoList {

    public static final String addTodoListModelID = "add-todo-list";
    public static final String addTodoListModelTitle = "title";
    public static final String addTodoListModelDescription = "description";

    public static void add(SlashCommandInteractionEvent e) {
        TextInput title = TextInput.create(addTodoListModelTitle, "Titel", TextInputStyle.SHORT)
                .setPlaceholder("Todo-List Title").setMinLength(5).setMaxLength(50).build();

        TextInput description = TextInput.create(addTodoListModelDescription, "Beschreibung", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Todo-List Beschreibung").setMinLength(15).setMaxLength(1000).build();

        Modal modal = Modal.create(addTodoListModelID, "Hinzufügen eines Todo-Listen eintrags!")
                .addActionRows(ActionRow.of(title), ActionRow.of(description)).build();
        e.replyModal(modal).queue();
    }

    public static void remove(SlashCommandInteractionEvent e) {
        TextChannel open = getTextChannel(e.getGuild(), MysqlConnection.clmOpenChannel);
        TextChannel closed = getTextChannel(e.getGuild(), MysqlConnection.clmClosedChannel);
        TextChannel waiting = getTextChannel(e.getGuild(), MysqlConnection.clmProcessChannel);

        String todoId = e.getOption(Discord.cmdTodoOptionTodo).getAsString();
        Message m = getMessageWithTodoID(open, closed, waiting, todoId);
        if (m != null) {
            m.delete().queue();
            e.reply("Das Todo wurde erfolgreich gelöscht!").setEphemeral(true).queue();
        } else {
            e.reply("Es konnte kein Todo mit dieser ID gefunden werden!").setEphemeral(true).queue();
        }
    }

    public static void setStatus(SlashCommandInteractionEvent e) {
        String todoId = e.getOption(Discord.cmdTodoOptionTodo).getAsString();
        String status = e.getOption(Discord.cmdTodoSubSetStatusOptionStatus).getAsString();

        TextChannel open = getTextChannel(e.getGuild(), MysqlConnection.clmOpenChannel);
        TextChannel closed = getTextChannel(e.getGuild(), MysqlConnection.clmClosedChannel);
        TextChannel waiting = getTextChannel(e.getGuild(), MysqlConnection.clmProcessChannel);


        if (open != null) {
            if (closed != null) {
                if (waiting != null) {
                    Message m = getMessageWithTodoID(open, closed, waiting, todoId);
                    if (m != null) {
                        String titel = m.getEmbeds().get(0).getTitle();
                        String description = m.getEmbeds().get(0).getDescription();

                        String id = null;
                        for (MessageEmbed.Field field : m.getEmbeds().get(0).getFields()) {
                            if (field.getName().equalsIgnoreCase("Todo-ID")) {
                                id = field.getValue();
                            }
                        }

                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setTitle(titel, "https://Golden-Developer.de");
                        eb.setDescription(description);
                        eb.setTimestamp(new Date().toInstant());
                        eb.addField("Zuletzt aktualisiert", "Von: " + e.getUser().getAsMention(), false);
                        eb.setFooter("@Golden-Developer", e.getJDA().getSelfUser().getAvatarUrl());
                        if (id != null) {
                            eb.addField("Todo-ID", id, false);
                        }
                        switch (status) {
                            case "open" -> {
                                eb.setColor(Color.GREEN);
                                open.sendMessageEmbeds(eb.build()).queue();
                            }
                            case "closed" -> {
                                eb.setColor(Color.red);
                                closed.sendMessageEmbeds(eb.build()).queue();
                            }
                            case "waiting" -> {
                                eb.setColor(Color.CYAN);
                                waiting.sendMessageEmbeds(eb.build()).queue();
                            }
                        }
                        m.delete().queue();
                        e.reply("Der Todo Status wurde erfolgreich aktualisiert!").setEphemeral(true).queue();
                    } else {
                        e.reply("Es konnte kein Todo mit dieser ID gefunden werden!").setEphemeral(true).queue();
                    }
                } else {
                    e.reply("ERROR: Der Channel für die in Bearbeitung Todos wurde noch nicht gesetzt! Nutze /settings set-prozess-channel").queue();
                }
            } else {
                e.reply("ERROR: Der Channel für die Geschlossenen Todos wurde noch nicht gesetzt! Nutze /settings set-closed-channel").queue();
            }
        } else {
            e.reply("ERROR: Der Channel für die Offenen Todos wurde noch nicht gesetzt! Nutze /settings set-open-channel").queue();
        }
    }

    public static Message getMessageWithTodoID(TextChannel open, TextChannel closed, TextChannel waiting, String todoID) {
        Message m = getMessage(open, todoID);
        if (m != null) {
            return m;
        } else {
            m = getMessage(closed, todoID);
            if (m != null) {
                return m;
            } else {
                m = getMessage(waiting, todoID);
                return m;
            }
        }
    }

    public static Message getMessage(TextChannel channel, String todoID) {
        MessageHistory history = MessageHistory.getHistoryFromBeginning(channel).complete();
        for (Message m : history.getRetrievedHistory()) {
            if (!m.getEmbeds().isEmpty()) {
                for (MessageEmbed embed : m.getEmbeds()) {
                    for (MessageEmbed.Field field : embed.getFields()) {
                        if (field.getName().equalsIgnoreCase("Todo-ID")) {
                            if (field.getValue().equalsIgnoreCase(todoID)) {
                                return m;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public static TextChannel getTextChannel(Guild guild, String column) {
        Table table = Main.getMysqlConnection().getMysql().getDatabase(MysqlConnection.dbName).getTable(MysqlConnection.settingTable);
        HashMap<String, SearchResult> row = table.getRow(table.getColumn(MysqlConnection.clmGuildID), guild.getId()).get();
        return guild.getTextChannelById(row.get(column).toString());
    }
}
