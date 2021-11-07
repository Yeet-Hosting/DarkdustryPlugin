package pandorum.comp;

import arc.Events;
import arc.util.Reflect;
import arc.util.Timer;
import mindustry.core.NetServer;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.net.Administration;
import mindustry.net.Packets.ConnectPacket;
import org.javacord.api.entity.activity.ActivityType;
import pandorum.PandorumPlugin;
import pandorum.discord.BotMain;
import pandorum.events.*;
import pandorum.events.filters.ActionFilter;
import pandorum.events.filters.ChatFilter;

import static mindustry.Vars.net;
import static mindustry.Vars.netServer;
import static pandorum.discord.BotMain.bot;

public class Loader {
    public static void init() {

        PandorumPlugin.writeBuffer = Reflect.get(NetServer.class, netServer, "writeBuffer");
        PandorumPlugin.outputBuffer = Reflect.get(NetServer.class, netServer, "outputBuffer");

        net.handleServer(ConnectPacket.class, ConnectHandler::handle);

        netServer.admins.addActionFilter(ActionFilter::filter);
        netServer.admins.addChatFilter(ChatFilter::filter);
        netServer.invalidHandler = InvalidCommandResponse::response;

        Events.on(EventType.PlayerUnbanEvent.class, PlayerUnbanListener::call);
        Events.on(EventType.PlayerBanEvent.class, PlayerBanListener::call);
        Events.on(EventType.ServerLoadEvent.class, ServerLoadListener::call);
        Events.on(EventType.WorldLoadEvent.class, WorldLoadListener::call);
        Events.on(EventType.BlockBuildEndEvent.class, BlockBuildEndListener::call);
        Events.on(EventType.ConfigEvent.class, ConfigListener::call);
        Events.on(EventType.TapEvent.class, TapListener::call);
        Events.on(EventType.DepositEvent.class, DepositListener::call);
        Events.on(EventType.WithdrawEvent.class, WithdrawListener::call);
        Events.on(EventType.BuildSelectEvent.class, BuildSelectListener::call);
        Events.on(EventType.PlayerJoin.class, PlayerJoinListener::call);
        Events.on(EventType.PlayerLeave.class, PlayerLeaveListener::call);
        Events.on(EventType.GameOverEvent.class, GameOverListener::call);
        Events.on(EventType.WaveEvent.class, WaveEventListener::call);
        Events.on(EventType.AdminRequestEvent.class, AdminRequestListener::call);
        Events.run(EventType.Trigger.update, TriggerUpdateListener::update);

        Administration.Config.motd.set("off");
        Administration.Config.messageRateLimit.set(1);
        Administration.Config.showConnectMessages.set(false);
        Administration.Config.strict.set(true);
        Administration.Config.enableVotekick.set(true);
        Administration.Config.allowCustomClients.set(true);

        PandorumPlugin.antiVPN = new AntiVPN("w7j425-826177-597253-3134u9");

        Effects.init();
        MenuListener.init();
        Icons.init();
        Ranks.init();
        BotMain.start();

        Timer.schedule(() -> bot.updateActivity(ActivityType.WATCHING, "Игроков на сервере: " + Groups.player.size()), 0f, 15f);
    }
}
