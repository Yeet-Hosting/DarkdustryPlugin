package pandorum.events;

import mindustry.game.EventType.WorldLoadEvent;
import pandorum.struct.CacheSeq;
import pandorum.struct.Seqs;

import java.time.Duration;

import static mindustry.Vars.world;
import static pandorum.PluginVars.config;
import static pandorum.PluginVars.history;

public class WorldLoadListener {

    @SuppressWarnings("unchecked")
    public static void call(final WorldLoadEvent event) {
        if (config.historyEnabled()) {
            history = new CacheSeq[world.width()][world.height()];
            world.tiles.eachTile(tile -> history[tile.x][tile.y] = Seqs.seqBuilder().maximumSize(config.historyLimit).expireAfterWrite(Duration.ofMillis(config.expireDelay)).build());
        }
    }
}
