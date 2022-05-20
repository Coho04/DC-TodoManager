package de.goldendeveloper.todomanager.discord;

import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import de.goldendeveloper.todomanager.Main;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

public class Discord {

    private JDA bot;

    public static String getCmdShutdown = "shutdown";
    public static String getCmdRestart = "restart";
    public static String cmdHelp = "help";

    public static final String cmdSettings = "settings";
    public static final String cmdSettingsSubCmdSetProcessChannel = "set-process-channel";
    public static final String cmdSettingsSubCmdSetClosedChannel = "set-closed-channel";
    public static final String cmdSettingsSubCmdSetOpenChannel = "set-open-channel";
    public static final String cmdSettingsSubCmdOptionChannel = "textchannel";

    public static final String cmdTodo = "todo-list";
    public static final String cmdTodoSubAdd = "add";
    public static final String cmdTodoSubRemove = "remove";
    public static final String cmdTodoSubSetStatus = "set-status";
    public static final String cmdTodoOptionTodo = "todo-id";
    public static final String cmdTodoSubSetStatusOptionStatus = "status";

    public Discord(String Token) {
        try {
            bot = JDABuilder.createDefault(Token)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.ROLE_TAGS, CacheFlag.EMOTE, CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS, CacheFlag.VOICE_STATE)
                    .enableIntents(GatewayIntent.GUILD_MESSAGE_REACTIONS,
                            GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_EMOJIS,
                            GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_PRESENCES,
                            GatewayIntent.GUILD_BANS, GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                            GatewayIntent.GUILD_INVITES, GatewayIntent.DIRECT_MESSAGE_TYPING,
                            GatewayIntent.GUILD_MESSAGE_TYPING, GatewayIntent.GUILD_VOICE_STATES,
                            GatewayIntent.GUILD_WEBHOOKS, GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_MESSAGE_TYPING)
                    .addEventListeners(new Events())
                    .setAutoReconnect(true)
                    .build().awaitReady();
            registerCommands();
            if (!System.getProperty("os.name").split(" ")[0].equalsIgnoreCase("windows")) {
                Online();
            }
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void registerCommands() {
        bot.upsertCommand(cmdHelp, "Zeigt dir eine Liste möglicher Befehle an!").queue();
        bot.upsertCommand(getCmdShutdown, "Fährt den Discord Bot herunter!").queue();
        bot.upsertCommand(getCmdRestart, "Startet den Discord Bot neu!").queue();
        bot.upsertCommand(cmdTodo, "Managed die Server Todo-List!")
                .addSubcommands(
                        new SubcommandData(cmdTodoSubAdd, "Fügt eine neue Aufgabe der Todo-List hinzu!"),
                        new SubcommandData(cmdTodoSubRemove, "Entfernt eine vorhandene Aufgabe von der Todo-List!").addOption(OptionType.STRING, cmdTodoOptionTodo, "Hier die Todo-ID eintragen!", true),
                        new SubcommandData(cmdTodoSubSetStatus, "Setzt den Status einer vorhandenen Aufgabe auf der Todo-List!")
                                .addOption(OptionType.STRING, cmdTodoSubSetStatusOptionStatus, "Hier den neun Status des Todos eintagen!", true, true)
                                .addOption(OptionType.STRING, cmdTodoOptionTodo, "Hier die Todo-ID eintragen!", true)
                ).queue();
        bot.upsertCommand(cmdSettings, "Stellt den Discord Bot für diesen Server ein!")
                .addSubcommands(
                        new SubcommandData(cmdSettingsSubCmdSetProcessChannel, "Setzt den Todo Process Channel!").addOption(OptionType.CHANNEL, cmdSettingsSubCmdOptionChannel, "Der Channel in den die Aufgaben gesendet werden sollen!", true),
                        new SubcommandData(cmdSettingsSubCmdSetClosedChannel, "Setzt den Todo Closed Channel!").addOption(OptionType.CHANNEL, cmdSettingsSubCmdOptionChannel, "Der Channel in den die Aufgaben gesendet werden sollen!", true),
                        new SubcommandData(cmdSettingsSubCmdSetOpenChannel, "Setzt den Todo Open Channel!").addOption(OptionType.CHANNEL, cmdSettingsSubCmdOptionChannel, "Der Channel in den die Aufgaben gesendet werden sollen!", true)
                ).queue();
    }

    private void Online() {
        WebhookEmbedBuilder embed = new WebhookEmbedBuilder();
        embed.setAuthor(new WebhookEmbed.EmbedAuthor(getBot().getSelfUser().getName(), getBot().getSelfUser().getAvatarUrl(), "https://Golden-Developer.de"));
        embed.setColor(0x00FF00);
        embed.addField(new WebhookEmbed.EmbedField(false, "[Status]", "ONLINE"));
        embed.addField(new WebhookEmbed.EmbedField(false, "Gestartet als", bot.getSelfUser().getName()));
        embed.addField(new WebhookEmbed.EmbedField(false, "Server", Integer.toString(bot.getGuilds().size())));
        embed.addField(new WebhookEmbed.EmbedField(false, "Status", "\uD83D\uDFE2 Gestartet"));
        embed.addField(new WebhookEmbed.EmbedField(false, "Version", getProjektVersion()));
        embed.setFooter(new WebhookEmbed.EmbedFooter("@Golden-Developer", getBot().getSelfUser().getAvatarUrl()));
        embed.setTimestamp(new Date().toInstant());
        new WebhookClientBuilder(Main.getConfig().getDiscordWebhook()).build().send(embed.build());
    }

    public String getProjektVersion() {
        Properties properties = new Properties();
        try {
            properties.load(this.getClass().getClassLoader().getResourceAsStream("project.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties.getProperty("version");
    }

    public JDA getBot() {
        return bot;
    }
}

