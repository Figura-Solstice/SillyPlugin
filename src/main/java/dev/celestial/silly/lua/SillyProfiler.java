package dev.celestial.silly.lua;

import com.llamalad7.mixinextras.lib.apache.commons.tuple.Pair;
import dev.celestial.silly.SillyUtil;
import dev.celestial.silly.helper.CyclicalDeque;
import org.figuramc.figura.lua.LuaWhitelist;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@LuaWhitelist
public class SillyProfiler {
    public CyclicalDeque<Pair<Long, Long>> lastTickEventTimes = new CyclicalDeque<>(60);
    public CyclicalDeque<Pair<Long, Long>> lastRenderEventTimes = new CyclicalDeque<>(60);
    public CyclicalDeque<Pair<Long, Long>> lastRenderTimes = new CyclicalDeque<>(60);
    public CyclicalDeque<Pair<Long, Long>> lastWorldTickTimes = new CyclicalDeque<>(60);
    public CyclicalDeque<Pair<Long, Long>> lastWorldRenderTimes = new CyclicalDeque<>(60);

    private LuaTable getData(CyclicalDeque<Pair<Long, Long>> longJohns) {
        LuaTable ret = new LuaTable();

        Pair<Long, Long>[] values = longJohns.toArray(new Pair[0]);
        int i = 1;
        for (Pair<Long, Long> value : values) {
            ret.set(i, LuaValue.valueOf(getTime(value)));
            i++;
        }
        return ret;
    }

    private long getTime(Pair<Long, Long> entry) {
        return entry.getRight() - entry.getLeft();
    }

    // { [string]: long[60] (nanos) }
    @LuaWhitelist
    public LuaTable getTimes(Boolean onlyLast) {
        LuaTable table = new LuaTable();
        if (onlyLast) {
            table.set("TICK_EVENT", getTime(lastTickEventTimes.getFirst()));
            table.set("RENDER_EVENT", getTime(lastRenderEventTimes.getFirst()));
            table.set("RENDER", getTime(lastRenderTimes.getFirst()));
            table.set("WORLD_TICK", getTime(lastWorldTickTimes.getFirst()));
            table.set("WORLD_RENDER", getTime(lastWorldRenderTimes.getFirst()));
        } else {
            table.set("TICK_EVENT", getTickTimes());
            table.set("RENDER_EVENT", getRenderEventTimes());
            table.set("RENDER", getRenderTimes());
            table.set("WORLD_TICK", getWorldTickTimes());
            table.set("WORLD_RENDER", getWorldRenderTimes());
        }
        return table;
    }

    // long[60] (nanos)
    @LuaWhitelist
    public LuaTable getTickTimes() {
        return getData(lastTickEventTimes);
    }

    // long[60] (nanos)
    @LuaWhitelist
    public LuaTable getRenderEventTimes() {
        return getData(lastRenderEventTimes);
    }

    // long[60] (nanos)
    @LuaWhitelist
    public LuaTable getRenderTimes() {
        return getData(lastRenderTimes);
    }

    @LuaWhitelist
    public LuaTable getWorldTickTimes() {
        return getData(lastWorldTickTimes);
    }

    @LuaWhitelist
    public LuaTable getWorldRenderTimes() {
        return getData(lastWorldRenderTimes);
    }

}
