package pandorum.commands.client;

import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import pandorum.components.Bundle;
import pandorum.listeners.handlers.MenuHandler;

import static mindustry.Vars.state;
import static pandorum.util.Search.findLocale;
import static pandorum.util.Utils.coloredTeam;

public class DespawnCommand implements CommandRunner<Player> {
    public void accept(String[] args, Player player) {
        Call.menu(player.con,
                MenuHandler.despawnMenu,
                Bundle.format("commands.admin.despawn.menu.header", findLocale(player.locale)),
                Bundle.format("commands.admin.despawn.menu.content", findLocale(player.locale), Groups.unit.size()),
                new String[][] {{Bundle.format("ui.menus.yes", findLocale(player.locale)), Bundle.format("ui.menus.no", findLocale(player.locale))}, {Bundle.format("commands.admin.despawn.menu.players", findLocale(player.locale))}, {Bundle.format("commands.admin.despawn.menu.team", findLocale(player.locale), coloredTeam(state.rules.defaultTeam))}, {Bundle.format("commands.admin.despawn.menu.team", findLocale(player.locale), coloredTeam(state.rules.waveTeam))}, {Bundle.format("commands.admin.despawn.menu.suicide", findLocale(player.locale))}}
        );
    }
}
