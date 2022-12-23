package darkdustry.features.menus;

import arc.struct.*;
import mindustry.gen.Player;

import static darkdustry.features.menus.MenuHandler.showMenu;
import static darkdustry.utils.Administration.ban;

public class TempBanMenu {

    public static final IntSeq durations = IntSeq.with(1, 3, 5, 7, 14, 30, 0);

    public static void show(Player admin, Player target) {
        showMenu(admin, "tempban.header", "tempban.content", new String[][] {
                {"tempban.1", "tempban.3", "tempban.5"},
                {"tempban.7", "tempban.14", "tempban.30"},
                {"tempban.permanent"},
        }, option -> selection(admin, target, option), target.coloredName());
    }

    public static void selection(Player admin, Player target, int option) {
        if (!admin.admin) return;

        ban(admin, target, option < 0 || option >= durations.size ? 0L : durations.get(option) * 24 * 60 * 60 * 1000L);
    }
}