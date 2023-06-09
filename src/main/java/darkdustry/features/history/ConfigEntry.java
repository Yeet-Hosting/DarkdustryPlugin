package darkdustry.features.history;

import arc.math.geom.Point2;
import arc.util.*;
import darkdustry.components.Icons;
import mindustry.ctype.UnlockableContent;
import mindustry.game.EventType.ConfigEvent;
import mindustry.gen.Player;
import mindustry.world.blocks.logic.*;
import mindustry.world.blocks.power.LightBlock;
import mindustry.world.blocks.units.UnitFactory;
import useful.Bundle;

import java.util.Arrays;

import static darkdustry.utils.Utils.*;
import static mindustry.Vars.*;

public class ConfigEntry implements HistoryEntry {

    public final String uuid;
    public final short blockID;
    public final Object value;
    public final boolean connect;
    public final long timestamp;

    public ConfigEntry(ConfigEvent event) {
        this.uuid = event.player.uuid();
        this.blockID = event.tile.block.id;
        this.value = getValue(event);
        this.connect = value instanceof Point2 point && getConnect(event, point);
        this.timestamp = Time.millis();
    }

    // Ифы сила, Дарк могила
    // (C) Овлер, 2021 год до н.э.
    @Override
    public String getMessage(Player player) {
        var info = netServer.admins.getInfo(uuid);
        var block = content.block(blockID);
        var time = formatTime(timestamp);

        if (value instanceof UnlockableContent content) {
            return Bundle.format("history.config", player, info.lastName, Icons.icon(block), Icons.icon(content), time);
        }

        if (value instanceof Boolean on) {
            return on ? Bundle.format("history.config.on", player, info.lastName, Icons.icon(block), time) : Bundle.format("history.config.off", player, info.lastName, Icons.icon(block), time);
        }

        if (value instanceof String text) {
            return !text.isEmpty() ? Bundle.format("history.config.text", player, info.lastName, Icons.icon(block), text, time) : Bundle.format("history.config.default", player, info.lastName, Icons.icon(block), time);
        }

        if (value instanceof Point2 point) {
            return connect ? Bundle.format("history.config.connect", player, info.lastName, Icons.icon(block), point.x, point.y, time) : Bundle.format("history.config.disconnect", player, info.lastName, Icons.icon(block), time);
        }

        if (value instanceof Point2[] points) {
            return points.length > 0 ? Bundle.format("history.config.connects", player, info.lastName, Icons.icon(block), Arrays.toString(points), time) : Bundle.format("history.config.disconnect", player, info.lastName, Icons.icon(block), time);
        }

        if (block instanceof LightBlock) {
            return Bundle.format("history.config.color", player, info.lastName, Icons.icon(block), Tmp.c1.set((int) value).toString(), time);
        }

        if (block instanceof LogicBlock) {
            return Bundle.format("history.config.code", player, info.lastName, Icons.icon(block), time);
        }

        if (block instanceof CanvasBlock) {
            return Bundle.format("history.config.image", player, info.lastName, Icons.icon(block), time);
        }

        return Bundle.format("history.config.default", player, info.lastName, Icons.icon(block), time);
    }

    public Object getValue(ConfigEvent event) {
        if (event.tile.block instanceof LogicBlock || event.tile.block instanceof CanvasBlock)
            return null;

        if (event.value instanceof Integer number) {
            if (event.tile.block instanceof UnitFactory factory)
                return number < 0 || number >= factory.plans.size ? null : factory.plans.get(number).unit;
            if (event.tile.block.configurations.containsKey(Point2.class) || event.tile.block.configurations.containsKey(Point2[].class))
                return Point2.unpack(number);
        }

        if (event.value instanceof Point2 point)
            return point.add(event.tile.tileX(), event.tile.tileY());

        if (event.value instanceof Point2[] points) {
            Structs.each(point -> point.add(event.tile.tileX(), event.tile.tileY()), points);
            return points;
        }

        return event.value;
    }

    public boolean getConnect(ConfigEvent event, Point2 point) {
        if (event.tile.block.configurations.containsKey(Point2.class))
            return point.pack() != -1 && point.pack() != event.tile.pos();

        if (event.tile.block.configurations.containsKey(Point2[].class))
            return Structs.contains((Point2[]) event.tile.config(), point.cpy().sub(event.tile.tileX(), event.tile.tileY()));

        return false;
    }
}