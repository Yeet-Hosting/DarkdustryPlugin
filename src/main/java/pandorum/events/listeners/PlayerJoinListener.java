package pandorum.events.listeners;

import arc.util.Log;
import arc.util.Strings;
import mindustry.game.EventType.PlayerJoin;
import mindustry.gen.Call;
import net.dv8tion.jda.api.EmbedBuilder;
import pandorum.comp.Bundle;
import pandorum.comp.Effects;
import pandorum.comp.Ranks;
import pandorum.events.handlers.MenuHandler;
import pandorum.models.PlayerModel;
import pandorum.util.Utils;

import java.awt.*;

import static pandorum.PluginVars.discordServerUrl;
import static pandorum.discord.Bot.botChannel;
import static pandorum.util.Search.findLocale;

public class PlayerJoinListener {

    public static void call(final PlayerJoin event) {
        Ranks.updateName(event.player, name -> {
            event.player.name(name);
            Log.info("@ зашел на сервер. [@]", name, event.player.uuid());
            Utils.sendToChat("events.player.join", name);
            botChannel.sendMessageEmbeds(new EmbedBuilder().setTitle(Strings.format("@ зашел на сервер.", Strings.stripColors(event.player.name))).setColor(Color.green).build()).queue();
        });

        if (event.player.bestCore() != null) Effects.onJoin(event.player.bestCore().x, event.player.bestCore().y);

        PlayerModel.find(event.player, playerInfo -> {
            if (playerInfo.welcomeMessage) Call.menu(event.player.con,
                    MenuHandler.welcomeMenu,
                    Bundle.format("events.welcome.menu.header", findLocale(event.player.locale)),
                    Bundle.format("events.welcome.menu.message", findLocale(event.player.locale), discordServerUrl),
                    new String[][] {{Bundle.format("events.welcome.menu.close", findLocale(event.player.locale))}, {Bundle.format("events.welcome.menu.disable", findLocale(event.player.locale))}}
            );
        });

        Utils.bundled(event.player, "events.welcome.message", discordServerUrl);
    }
}
