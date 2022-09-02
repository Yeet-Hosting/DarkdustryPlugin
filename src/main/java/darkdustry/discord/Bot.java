package darkdustry.discord;

import darkdustry.DarkdustryPlugin;
import darkdustry.commands.DiscordCommands;
import darkdustry.utils.Find;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.utils.messages.MessageRequest;

import java.awt.*;
import java.util.EnumSet;
import java.util.Objects;

import static arc.util.Strings.format;
import static arc.util.Strings.stripColors;
import static darkdustry.PluginVars.config;
import static darkdustry.PluginVars.loginWaiting;
import static darkdustry.components.Bundle.bundled;
import static darkdustry.components.Bundle.format;
import static darkdustry.discord.Bot.Palette.*;
import static darkdustry.features.Authme.menu;
import static mindustry.Vars.state;
import static net.dv8tion.jda.api.Permission.ADMINISTRATOR;
import static net.dv8tion.jda.api.entities.Activity.watching;
import static net.dv8tion.jda.api.entities.Message.MentionType.CHANNEL;
import static net.dv8tion.jda.api.entities.Message.MentionType.EMOJI;
import static net.dv8tion.jda.api.interactions.components.ActionRow.of;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS;
import static net.dv8tion.jda.api.requests.GatewayIntent.MESSAGE_CONTENT;

public class Bot {

    public static JDA jda;

    public static Guild botGuild;
    public static Role adminRole;
    public static MessageChannel botChannel, adminChannel;

    public static void connect() {
        try {
            jda = JDABuilder.createLight(config.discordBotToken)
                    .enableIntents(GUILD_MEMBERS, MESSAGE_CONTENT)
                    .addEventListeners(new DiscordListeners()).build().awaitReady();

            botGuild = jda.getGuildById(config.discordGuildId);
            adminRole = Objects.requireNonNull(botGuild).getRoleById(config.discordAdminRoleId);
            botChannel = botGuild.getTextChannelById(config.discordBotChannelId);
            adminChannel = botGuild.getTextChannelById(config.discordAdminChannelId);

            MessageRequest.setDefaultMentions(EnumSet.of(CHANNEL, EMOJI));

            botGuild.updateCommands().queue();
            DiscordCommands.load();

            updateBotStatus();

            DarkdustryPlugin.info("Bot connected. (@)", jda.getSelfUser().getAsTag());
        } catch (Exception e) {
            DarkdustryPlugin.error("Failed to connect bot: @", e);
        }
    }

    public static void exit() {
        jda.shutdown();
    }

    public static void sendMessageToGame(Member member, Message message) {
        DarkdustryPlugin.discord("@: @", member.getEffectiveName(), message.getContentDisplay());

        var roles = member.getRoles();
        var reply = message.getReferencedMessage();

        Groups.player.each(player -> {
            var locale = Find.locale(player.locale);

            bundled(player, "discord.chat",
                    Integer.toHexString(member.getColorRaw()),
                    roles.isEmpty() ? format("discord.chat.no-role", locale) : roles.get(0).getName(),
                    member.getEffectiveName(),
                    reply != null ? format("discord.chat.reply", locale, botGuild.retrieveMember(reply.getAuthor()).complete().getEffectiveName()) : "",
                    message.getContentDisplay());
        });
    }

    public static void sendAdminRequest(Player player) {
        adminChannel.sendMessageEmbeds(neutral(":eyes: Запрос на получение прав администратора.")
                .addField("Никнейм:", player.plainName(), true)
                .addField("UUID:", player.uuid(), true)
                .setFooter("Выберите нужную опцию, чтобы подтвердить или отклонить запрос. Подтверждайте только свои запросы!")
                .build()
        ).setComponents(of(menu)).queue(message -> loginWaiting.put(message, player.uuid()));
    }

    public static boolean isAdmin(Member member) {
        return member != null && (member.getRoles().contains(adminRole) || member.hasPermission(ADMINISTRATOR));
    }

    public static void updateBotStatus() {
        jda.getPresence().setActivity(watching("на " + Groups.player.size() + " игроков на карте " + stripColors(state.map.name())));
    }

    public static void sendMessage(MessageChannel channel, String text, Object... args) {
        channel.sendMessage(format(text, args)).queue();
    }

    public static void sendEmbed(MessageChannel channel, Color color, String text, Object... args) {
        channel.sendMessageEmbeds(new EmbedBuilder().setColor(color).setTitle(format(text, args)).build()).queue();
    }

    public static EmbedBuilder success(String title) {
        return new EmbedBuilder().setColor(SUCCESS).setTitle(title);
    }

    public static EmbedBuilder success(String title, String description, Object... args) {
        return success(title).setDescription(format(description, args));
    }

    public static EmbedBuilder info(String title) {
        return new EmbedBuilder().setColor(INFO).setTitle(title);
    }

    public static EmbedBuilder info(String title, String description, Object... args) {
        return info(title).setDescription(format(description, args));
    }

    public static EmbedBuilder error(String title) {
        return new EmbedBuilder().setColor(ERROR).setTitle(title);
    }

    public static EmbedBuilder error(String title, String description, Object... args) {
        return error(title).setDescription(format(description, args));
    }

    public static EmbedBuilder neutral(String title) {
        return new EmbedBuilder().setColor(NEUTRAL).setTitle(title);
    }

    public static class Palette {
        public static Color
                SUCCESS = Color.decode("#3cfb63"),
                INFO = Color.decode("#fcf47c"),
                ERROR = Color.decode("#f93c3c"),
                NEUTRAL = Color.decode("#2c94ec");
    }
}
