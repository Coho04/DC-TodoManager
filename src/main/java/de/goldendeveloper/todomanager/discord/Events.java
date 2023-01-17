package de.goldendeveloper.todomanager.discord;

import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import de.goldendeveloper.mysql.entities.RowBuilder;
import de.goldendeveloper.mysql.entities.SearchResult;
import de.goldendeveloper.mysql.entities.Table;
import de.goldendeveloper.todomanager.Main;
import de.goldendeveloper.todomanager.MysqlConnection;
import de.goldendeveloper.todomanager.discord.utility.TodoList;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;
import java.util.Date;

public class Events extends ListenerAdapter {

    @Override
    public void onShutdown(@NotNull ShutdownEvent e) {
        if (Main.getDeployment()) {
            WebhookEmbedBuilder embed = new WebhookEmbedBuilder();
            embed.setAuthor(new WebhookEmbed.EmbedAuthor(Main.getDiscord().getBot().getSelfUser().getName(), Main.getDiscord().getBot().getSelfUser().getAvatarUrl(), "https://Golden-Developer.de"));
            embed.addField(new WebhookEmbed.EmbedField(false, "[Status]", "Offline"));
            embed.addField(new WebhookEmbed.EmbedField(false, "Gestoppt als", Main.getDiscord().getBot().getSelfUser().getName()));
            embed.addField(new WebhookEmbed.EmbedField(false, "Server", Integer.toString(Main.getDiscord().getBot().getGuilds().size())));
            embed.addField(new WebhookEmbed.EmbedField(false, "Status", "\uD83D\uDD34 Offline"));
            embed.addField(new WebhookEmbed.EmbedField(false, "Version", Main.getConfig().getProjektVersion()));
            embed.setFooter(new WebhookEmbed.EmbedFooter("@Golden-Developer", Main.getDiscord().getBot().getSelfUser().getAvatarUrl()));
            embed.setTimestamp(new Date().toInstant());
            embed.setColor(0xFF0000);
            new WebhookClientBuilder(Main.getConfig().getDiscordWebhook()).build().send(embed.build()).thenRun(() -> System.exit(0));
        }
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent e) {
        Main.getServerCommunicator().removeServer(e.getGuild().getId());
        e.getJDA().getPresence().setActivity(Activity.playing("/help | " + e.getJDA().getGuilds().size() + " Servern"));
    }


    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
        User _Coho04_ = e.getJDA().getUserById("513306244371447828");
        User zRazzer = e.getJDA().getUserById("428811057700536331");
        Table table = Main.getMysqlConnection().getMysql().getDatabase(MysqlConnection.dbName).getTable(MysqlConnection.settingTable);
        SearchResult roleID = table.getRow(table.getColumn(MysqlConnection.clmGuildID), e.getGuild().getId()).get().get(MysqlConnection.clmPermRole);
        String cmd = e.getName();
        if (e.isFromGuild()) {
            Role role = e.getGuild().getRoleById(roleID.getAsString());
            if (hasRole(role, e.getMember())) {
                if (cmd.equalsIgnoreCase(Discord.cmdTodo)) {
                    if (e.getSubcommandName() != null) {
                        switch (e.getSubcommandName()) {
                            case Discord.cmdTodoSubAdd -> TodoList.add(e);
                            case Discord.cmdTodoSubRemove -> TodoList.remove(e);
                            case Discord.cmdTodoSubSetStatus -> TodoList.setStatus(e);
                        }
                    }
                } else if (cmd.equalsIgnoreCase(Discord.cmdSettings)) {
                    if (e.getSubcommandName() != null) {
                        switch (e.getSubcommandName()) {
                            case Discord.cmdSettingsSubCmdSetClosedChannel -> setChannel(e, "closed");
                            case Discord.cmdSettingsSubCmdSetOpenChannel -> setChannel(e, "open");
                            case Discord.cmdSettingsSubCmdSetProcessChannel -> setChannel(e, "process");
                        }
                    }
                }
            } else {
                e.reply("Dazu hast du keine Rechte! Dir fehlt die Rolle: " + role.getName()).queue();
            }

            if (e.getName().equalsIgnoreCase(Discord.getCmdShutdown)) {
                if (e.getUser() == zRazzer || e.getUser() == _Coho04_) {
                    e.getInteraction().reply("Der Bot wird nun heruntergefahren").queue();
                    e.getJDA().shutdown();
                } else {
                    e.getInteraction().reply("Dazu hast du keine Rechte du musst für diesen Befehl der Bot Inhaber sein!").queue();
                }
            } else if (e.getName().equalsIgnoreCase(Discord.getCmdRestart)) {
                if (e.getUser() == zRazzer || e.getUser() == _Coho04_) {
                    try {
                        e.getInteraction().reply("Der Discord Bot wird nun neugestartet!").queue();
                        Process p = Runtime.getRuntime().exec("screen -AmdS " + Main.getConfig().getProjektName() + " java -Xms1096M -Xmx1096M -jar " + Main.getConfig().getProjektName() + "-" + Main.getConfig().getProjektVersion() + ".jar restart");
                        p.waitFor();
                        e.getJDA().shutdown();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    e.getInteraction().reply("Dazu hast du keine Rechte du musst für diesen Befehl der Bot Inhaber sein!").queue();
                }
            } else if (cmd.equalsIgnoreCase(Discord.cmdHelp)) {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("**Help Commands**");
                embed.setColor(Color.MAGENTA);
                for (Command cm : Main.getDiscord().getBot().retrieveCommands().complete()) {
                    embed.addField("/" + cm.getName(), cm.getDescription(), true);
                }
                embed.setFooter("@Golden-Developer", e.getJDA().getSelfUser().getAvatarUrl());
                e.getInteraction().replyEmbeds(embed.build()).addActionRow(
                        Button.link("https://wiki.Golden-Developer.de/", "Online Übersicht"),
                        Button.link("https://support.Golden-Developer.de", "Support Anfragen")
                ).queue();
            }
        } else {
            e.reply("Dieser Command ist nur auf einem Server verfügbar!").queue();
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent e) {
        if (e.getName().equalsIgnoreCase(Discord.cmdTodo)) {
            if (e.getSubcommandName().equalsIgnoreCase(Discord.cmdTodoSubSetStatus)) {
                if (e.getFocusedOption().getName().equalsIgnoreCase(Discord.cmdTodoSubSetStatusOptionStatus)) {
                    e.replyChoices(
                            new Command.Choice("Offen", "open"),
                            new Command.Choice("Wartend", "waiting"),
                            new Command.Choice("Geschlossen", "closed")
                    ).queue();
                }
            }
        }
    }

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
            embedBuilder.setFooter("@Golden-Developer", e.getJDA().getSelfUser().getAvatarUrl());
            embedBuilder.setColor(Color.GREEN);
            embedBuilder.addField("Todo-ID", "#" + Instant.now().getEpochSecond(), false);
            e.reply("Das Todo wurde hinzugefügt!").setEphemeral(true).queue();
            TodoList.getTextChannel(e.getGuild(), MysqlConnection.clmOpenChannel).sendMessageEmbeds(embedBuilder.build()).queue();
        }
    }

    @Override
    public void onGuildJoin(GuildJoinEvent e) {
        Main.getServerCommunicator().addServer(e.getGuild().getId());
        e.getJDA().getPresence().setActivity(Activity.playing("/help | " + e.getJDA().getGuilds().size() + " Servern"));
        Table table = Main.getMysqlConnection().getMysql().getDatabase(MysqlConnection.dbName).getTable(MysqlConnection.settingTable);
        if (!table.getColumn(MysqlConnection.clmGuildID).getAll().getAsString().contains(e.getGuild().getId())) {
            e.getGuild().createRole().queue(role ->  {
                role.getManager().setName("Todo").queue();
                table.insert(
                        new RowBuilder()
                                .with(table.getColumn(MysqlConnection.clmGuildID), e.getGuild().getId())
                                .with(table.getColumn(MysqlConnection.clmOpenChannel), "")
                                .with(table.getColumn(MysqlConnection.clmProcessChannel), "")
                                .with(table.getColumn(MysqlConnection.clmClosedChannel), "")
                                .with(table.getColumn(MysqlConnection.clmPermRole), role.getId())
                                .build()
                );
            });
        }
    }


    public void setChannel(SlashCommandInteractionEvent e, String Channel) {
        TextChannel channel = e.getOption(Discord.cmdSettingsSubCmdOptionChannel).getAsChannel().asTextChannel();
        if (channel != null) {
            if (Main.getMysqlConnection().getMysql().existsDatabase(MysqlConnection.dbName)) {
                if (Main.getMysqlConnection().getMysql().getDatabase(MysqlConnection.dbName).existsTable(MysqlConnection.settingTable)) {
                    Table table = Main.getMysqlConnection().getMysql().getDatabase(MysqlConnection.dbName).getTable(MysqlConnection.settingTable);
                    if (table.getColumn(MysqlConnection.clmGuildID).getAll().getAsString().contains(e.getGuild().getId())) {
                        switch (Channel) {
                            case "closed" ->
                                    table.getRow(table.getColumn(MysqlConnection.clmGuildID), e.getGuild().getId()).set(table.getColumn(MysqlConnection.clmClosedChannel), channel.getId());
                            case "open" ->
                                    table.getRow(table.getColumn(MysqlConnection.clmGuildID), e.getGuild().getId()).set(table.getColumn(MysqlConnection.clmOpenChannel), channel.getId());
                            case "process" ->
                                    table.getRow(table.getColumn(MysqlConnection.clmGuildID), e.getGuild().getId()).set(table.getColumn(MysqlConnection.clmProcessChannel), channel.getId());
                        }
                        e.reply("Der Channel wurde erfolgreich eingestellt!").queue();
                    } else {
                        e.reply("ERROR: [404][Column in MysqlTable not Found] Lade den Bot neu auf deinen Server ein!!!").queue();
                    }
                }
            }
        } else {
            e.reply("ERROR: [404][TextChannel not found]").queue();
        }
    }

    public boolean hasRole(Role role, Member m) {
        for (Role r : m.getRoles()) {
            if (r == role) {
                return true;
            }
        }
        return false;
    }
}
