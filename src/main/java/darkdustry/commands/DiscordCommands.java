package darkdustry.commands;

import arc.Events;
import arc.files.Fi;
import arc.func.Cons;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import darkdustry.components.Config.Gamemode;
import darkdustry.discord.Bot;
import darkdustry.utils.Find;
import darkdustry.utils.PageIterator;
import mindustry.game.EventType.GameOverEvent;
import mindustry.gen.Groups;
import mindustry.net.Packets.KickReason;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import static arc.Core.app;
import static arc.Core.graphics;
import static arc.util.Time.timeSinceMillis;
import static darkdustry.PluginVars.*;
import static darkdustry.components.Bundle.sendToChat;
import static darkdustry.components.MapParser.renderMap;
import static darkdustry.components.MapParser.renderMinimap;
import static darkdustry.components.MenuHandler.showMenu;
import static darkdustry.discord.Bot.*;
import static darkdustry.utils.Checks.*;
import static darkdustry.utils.Utils.*;
import static java.util.Objects.requireNonNull;
import static mindustry.Vars.*;
import static mindustry.net.Administration.Config.serverName;
import static net.dv8tion.jda.api.Permission.BAN_MEMBERS;
import static net.dv8tion.jda.api.Permission.KICK_MEMBERS;
import static net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions.DISABLED;
import static net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions.enabledFor;
import static net.dv8tion.jda.api.interactions.commands.OptionType.*;
import static net.dv8tion.jda.api.utils.FileUpload.fromData;

public class DiscordCommands {

    public static final ObjectMap<String, Cons<SlashCommandInteractionEvent>> commands = new ObjectMap<>();
    public static final Seq<SlashCommandData> datas = new Seq<>();

    public static void load() {
        register("status", "Посмотреть статус сервера.", event -> {
            if (isMenu(event)) return;

            EmbedBuilder embed = info(":satellite: " + stripAll(serverName.string()), """
                            Игроков: @
                            Карта: @
                            Волна: @
                            TPS: @
                            Потребление ОЗУ: @ МБ
                            Время работы сервера: @
                            Время игры на текущей карте: @
                            """, Groups.player.size(), state.map.name(), state.wave,
                    graphics.getFramesPerSecond(), app.getJavaHeap() / 1024 / 1024,
                    formatDuration(timeSinceMillis(serverLoadTime)), formatDuration(timeSinceMillis(mapLoadTime)))
                    .setImage("attachment://minimap.png");

            event.replyEmbeds(embed.build())
                    .addFiles(fromData(renderMinimap(), "minimap.png"))
                    .queue();
        });


        register("discord", "Привязывает ваш дискорд к игровому аккаунту.", event -> {
            var target = Find.player(requireNonNull(event.getOption("name")).getAsString());
            if (notFound(event, target)) return;

            showMenu(target, 0, "discord.menu.link.header", "discord.menu.link.content", new String[][]{{"ui.menus.close"}, {"discord.button.link"}});
            linkWaiting.put(requireNonNull(event.getMember()).getId(), target.uuid());

            event.reply("Проверьте окно игры.").setEphemeral(true).queue();
        }).addOption(STRING, "name", "Имя игрока.", true);


        register("players", "Список всех игроков на сервере.", PageIterator::players)
                .addOption(INTEGER, "page", "Страница списка игроков.");


        register("kick", "Выгнать игрока с сервера.", event -> {
            var target = Find.player(requireNonNull(event.getOption("name")).getAsString());
            if (notFound(event, target)) return;

            kick(target, kickDuration, true, "kick.kicked");
            sendToChat("events.server.kick", target.coloredName());

            event.replyEmbeds(info(":candle: Игрок успешно выгнан с сервера.", "@ не сможет зайти на сервер в течение @", target.plainName(), formatDuration(kickDuration)).build()).queue();
        }).setDefaultPermissions(enabledFor(KICK_MEMBERS))
                .addOption(STRING, "name", "Имя игрока, которого нужно выгнать.", true);


        register("ban", "Забанить игрока на сервере.", event -> {
            var target = Find.player(requireNonNull(event.getOption("name")).getAsString());
            if (notFound(event, target)) return;

            netServer.admins.banPlayer(target.uuid());
            kick(target, 0, true, "kick.banned");
            sendToChat("events.server.ban", target.coloredName());

            event.replyEmbeds(info(":wheelchair: Игрок успешно заблокирован.", "@ больше не сможет зайти на сервер.", target.plainName()).build()).queue();
        }).setDefaultPermissions(enabledFor(BAN_MEMBERS))
                .addOption(STRING, "name", "Имя игрока, которого нужно забанить.", true);


        register("restart", "Перезапустить сервер.", event -> {
            // Сервер перезапустится только после отправки сообщения

            event.replyEmbeds(info(":gear: Сервер перезапускается...").build()).queue(hook -> {
                netServer.kickAll(KickReason.serverRestarting);
                app.post(Bot::exit);
                app.exit();
            });
        }).setDefaultPermissions(DISABLED);


        if (config.mode == Gamemode.hexed) return;

        register("map", "Получить карту с сервера.", event -> {
            var map = Find.map(requireNonNull(event.getOption("map")).getAsString());
            if (notFound(event, map)) return;

            EmbedBuilder embed = info(":map: " + map.name())
                    .setAuthor(map.tags.get("author"))
                    .setDescription(map.tags.get("description"))
                    .setFooter(map.width + "x" + map.height)
                    .setImage("attachment://map.png");

            event.replyEmbeds(embed.build())
                    .addFiles(fromData(map.file.file()))
                    .addFiles(fromData(renderMap(map), "map.png"))
                    .queue();
        }).addOption(STRING, "map", "Название карты, которую вы хотите получить.", true);


        register("maps", "Список всех карт сервера.", PageIterator::maps)
                .addOption(INTEGER, "page", "Страница списка карт.");


        register("addmap", "Добавить карту на сервер.", event -> {
            if (notMap(event)) return;

            var attachment = requireNonNull(event.getOption("map")).getAsAttachment();
            attachment.getProxy().downloadToFile(customMapDirectory.child(attachment.getFileName()).file()).thenAccept(file -> {
                if (notMap(event, new Fi(file))) return;
                event.replyEmbeds(success(":map: Карта добавлена на сервер.", "Файл карты: @", file.getName()).build()).queue();
            });
        }).setDefaultPermissions(DISABLED)
                .addOption(ATTACHMENT, "map", "Файл карты, которую необходимо загрузить на сервер.", true);


        register("removemap", "Удалить карту с сервера.", event -> {
            var map = Find.map(requireNonNull(event.getOption("map")).getAsString());
            if (notFound(event, map)) return;

            maps.removeMap(map);
            maps.reload();

            event.replyEmbeds(success(":knife: Карта удалена с сервера.", "Название карты: @", map.name()).build()).queue();
        }).setDefaultPermissions(DISABLED)
                .addOption(STRING, "map", "Название карты, которую необходимо удалить с сервера.", true);


        register("gameover", "Принудительно завершить игру.", event -> {
            if (isMenu(event)) return;

            Events.fire(new GameOverEvent(state.rules.waveTeam));
            event.replyEmbeds(success(":map: Игра успешно завершена.").build()).queue();
        }).setDefaultPermissions(DISABLED);


        // Регистрируем все команды одним запросом
        jda.updateCommands().addCommands(datas.toArray(CommandData.class)).queue();
    }

    public static SlashCommandData register(String name, String description, Cons<SlashCommandInteractionEvent> cons) {
        commands.put(name, cons);
        return datas.add(new CommandDataImpl(name, description)).peek();
    }
}
