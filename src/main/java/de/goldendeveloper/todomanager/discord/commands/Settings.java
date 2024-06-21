package de.goldendeveloper.todomanager.discord.commands;

import de.goldendeveloper.todomanager.Main;
import de.goldendeveloper.todomanager.MysqlConnection;
import de.goldendeveloper.todomanager.discord.utility.TodoTypes;
import io.github.coho04.dcbcore.DCBot;
import io.github.coho04.dcbcore.interfaces.CommandInterface;
import io.github.coho04.mysql.entities.Row;
import io.github.coho04.mysql.entities.Table;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class Settings implements CommandInterface {

    private static final String cmdSettings = "settings";
    private static final String subCmdSetProcessChannel = "set-process-channel";
    private static final String subCmdSetClosedChannel = "set-closed-channel";
    private static final String subCmdSetOpenChannel = "set-open-channel";
    private static final String subCmdOptionChannel = "textchannel";

    @Override
    public CommandData commandData() {
        return Commands.slash(cmdSettings, "Stellt den Discord Bot für diesen Server ein!")
                .setGuildOnly(true)
                .addSubcommands(
                        new SubcommandData(subCmdSetProcessChannel, "Setzt den Todo Process Channel!").addOption(OptionType.CHANNEL, subCmdOptionChannel, "Der Channel in den die Aufgaben gesendet werden sollen!", true),
                        new SubcommandData(subCmdSetClosedChannel, "Setzt den Todo Closed Channel!").addOption(OptionType.CHANNEL, subCmdOptionChannel, "Der Channel in den die Aufgaben gesendet werden sollen!", true),
                        new SubcommandData(subCmdSetOpenChannel, "Setzt den Todo Open Channel!").addOption(OptionType.CHANNEL, subCmdOptionChannel, "Der Channel in den die Aufgaben gesendet werden sollen!", true)
                );
    }

    @Override
    public void runSlashCommand(SlashCommandInteractionEvent e, DCBot dcBot) {
        Table table = Main.getMysqlConnection().getMysql().getDatabase(MysqlConnection.dbName).getTable(MysqlConnection.settingTable);
        String roleID = table.getRow(table.getColumn(MysqlConnection.clmGuildID), e.getGuild().getId()).getData().get(MysqlConnection.clmPermRole).getAsString();
        Role role = e.getGuild().getRoleById(roleID);
        if (hasRole(role, e.getMember()) && e.getSubcommandName() != null) {
            switch (e.getSubcommandName()) {
                case subCmdSetClosedChannel -> setChannel(e, TodoTypes.CLOSED);
                case subCmdSetOpenChannel -> setChannel(e, TodoTypes.OPEN);
                case subCmdSetProcessChannel -> setChannel(e, TodoTypes.WAITING);
            }
        } else {
            e.reply("Dazu hast du keine Rechte! Dir fehlt die Rolle: " + role.getName()).queue();
        }
    }

    public void setChannel(SlashCommandInteractionEvent e, TodoTypes type) {
        TextChannel textChannel = e.getOption(subCmdOptionChannel).getAsChannel().asTextChannel();
        if (Main.getMysqlConnection().getMysql().existsDatabase(MysqlConnection.dbName)) {
            if (Main.getMysqlConnection().getMysql().getDatabase(MysqlConnection.dbName).existsTable(MysqlConnection.settingTable)) {
                Table table = Main.getMysqlConnection().getMysql().getDatabase(MysqlConnection.dbName).getTable(MysqlConnection.settingTable);
                if (table.getColumn(MysqlConnection.clmGuildID).getAll().getAsString().contains(e.getGuild().getId())) {
                    Row row = table.getRow(table.getColumn(MysqlConnection.clmGuildID), e.getGuild().getId());
                    row.set(table.getColumn(type.getColumnName()), textChannel.getId());
                    e.reply("Der Channel wurde erfolgreich eingestellt!").queue();
                } else {
                    e.reply("ERROR: [404][Column in MysqlTable not Found] Lade den Bot neu auf deinen Server ein!!!").queue();
                }
            }
        }
    }
}
