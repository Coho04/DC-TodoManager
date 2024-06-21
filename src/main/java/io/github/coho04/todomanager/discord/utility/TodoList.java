package io.github.coho04.todomanager.discord.utility;

import io.github.coho04.todomanager.discord.commands.Todo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.util.Date;

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
                .addComponents(ActionRow.of(title), ActionRow.of(description)).build();
        e.replyModal(modal).queue();
    }

    public static void remove(SlashCommandInteractionEvent e) {
        String todoId = e.getOption(Todo.cmdTodoOptionTodo).getAsString();
        Message m = getMessageWithTodoID(e.getGuild(), todoId);
        if (m != null) {
            m.delete().queue();
            e.reply("Das Todo wurde erfolgreich gelöscht!").setEphemeral(true).queue();
        } else {
            e.reply("Es konnte kein Todo mit dieser ID gefunden werden!").setEphemeral(true).queue();
        }
    }

    public static void setStatus(SlashCommandInteractionEvent e) {
        String status = e.getOption(Todo.cmdTodoSubSetStatusOptionStatus).getAsString();
        TodoTypes todoType = TodoTypes.valueOf(status);
        if (TodoTypes.OPEN.getChannel(e.getGuild()) != null) {
            if (TodoTypes.CLOSED.getChannel(e.getGuild()) != null) {
                if (TodoTypes.WAITING.getChannel(e.getGuild()) != null) {
                    Message m = getMessageWithTodoID(e.getGuild(), e.getOption(Todo.cmdTodoOptionTodo).getAsString());
                    if (m != null) {
                        String titel = m.getEmbeds().get(0).getTitle();
                        String description = m.getEmbeds().get(0).getDescription();

                        String id = m.getEmbeds().get(0).getFields().stream()
                                .filter(field -> field.getName().equalsIgnoreCase("Todo-ID"))
                                .findFirst().map(MessageEmbed.Field::getValue).orElse(null);

                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setTitle(titel);
                        eb.setDescription(description);
                        eb.setTimestamp(new Date().toInstant());
                        eb.addField("Zuletzt aktualisiert", "Von: " + e.getUser().getAsMention(), false);
                        eb.setFooter("@TodoManager", e.getJDA().getSelfUser().getAvatarUrl());
                        if (id != null) {
                            eb.addField("Todo-ID", id, false);
                        }
                        eb.setColor(todoType.getColor());
                        todoType.getChannel(e.getGuild()).sendMessageEmbeds(eb.build()).queue();
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

    public static Message getMessageWithTodoID(Guild guild, String todoID) {
        Message message = getMessage(TodoTypes.OPEN.getChannel(guild), todoID);
        if (message != null) {
            return message;
        } else {
            message = getMessage(TodoTypes.CLOSED.getChannel(guild), todoID);
            if (message != null) {
                return message;
            } else {
                return getMessage(TodoTypes.WAITING.getChannel(guild), todoID);
            }
        }
    }

    public static Message getMessage(TextChannel channel, String todoID) {
        MessageHistory history = MessageHistory.getHistoryFromBeginning(channel).complete();
        for (Message m : history.getRetrievedHistory().stream().filter(message -> !message.getEmbeds().isEmpty()).toList()) {
            for (MessageEmbed embed : m.getEmbeds()) {
                for (MessageEmbed.Field field : embed.getFields().stream().filter(field -> field.getName().equalsIgnoreCase("Todo-ID") && field.getValue().equalsIgnoreCase(todoID)).toList()) {
                    return m;
                }
            }
        }
        return null;
    }
}
