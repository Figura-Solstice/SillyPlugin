package dev.celestial.silly.lua;

import com.llamalad7.mixinextras.lib.apache.commons.tuple.Pair;
import dev.celestial.silly.SillyUtil;
import dev.celestial.silly.helper.CyclicalDeque;
import dev.celestial.silly.not_a_mixin.AvatarExtensions;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.spongepowered.asm.mixin.injection.Inject;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@LuaWhitelist
@LuaTypeDoc(name = "SillyProfiler", value = "silly_profiler")
public class SillyProfiler {
    private final Avatar owner;
    public CyclicalDeque<Pair<Long, Long>> lastTickEventTimes = new CyclicalDeque<>(60);
    public CyclicalDeque<Pair<Long, Long>> lastRenderEventTimes = new CyclicalDeque<>(60);
    public CyclicalDeque<Pair<Long, Long>> lastRenderTimes = new CyclicalDeque<>(60);
    public CyclicalDeque<Pair<Long, Long>> lastWorldTickTimes = new CyclicalDeque<>(60);
    public CyclicalDeque<Pair<Long, Long>> lastWorldRenderTimes = new CyclicalDeque<>(60);

    public SillyProfiler(Avatar owner) {
        this.owner = owner;
    }

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

    private long getTime(CyclicalDeque<Pair<Long,Long>> deque) {
        if (deque.isEmpty())
            return -1;
        return getTime(deque.getFirst());
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly_profiler.get_memory_usage",
            overloads = {@LuaMethodOverload(returnType = Long.class)}
    )
    public long getMemoryUsage() {
        return ((AvatarExtensions)owner).silly$getAllocationTracker().getTotal();
    }

    // { [string]: long[60] (nanos) }
    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly_profiler.get_times",
            overloads = {
                    @LuaMethodOverload(
                            argumentNames = { "onlyLast" },
                            argumentTypes = { Boolean.class },
                            returnType = LuaTable.class
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {},
                            argumentNames = {},
                            returnType = LuaTable.class
                    )
            }
    )
    public LuaTable getTimes(Boolean onlyLast) {
        LuaTable table = new LuaTable();
        if (onlyLast == null) onlyLast = false;
        if (onlyLast) {
            table.set("TICK_EVENT", getTime(lastTickEventTimes));
            table.set("RENDER_EVENT", getTime(lastRenderEventTimes));
            table.set("RENDER", getTime(lastRenderTimes));
            table.set("WORLD_TICK", getTime(lastWorldTickTimes));
            table.set("WORLD_RENDER", getTime(lastWorldRenderTimes));
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
    @LuaMethodDoc(
            value = "silly_profiler.get_tick_times",
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = {},
                            argumentNames = {},
                            returnType = LuaTable.class
                    )
            }
    )
    public LuaTable getTickTimes() {
        return getData(lastTickEventTimes);
    }

    // long[60] (nanos)
    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly_profiler.get_render_event_times",
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = {},
                            argumentNames = {},
                            returnType = LuaTable.class
                    )
            }
    )
    public LuaTable getRenderEventTimes() {
        return getData(lastRenderEventTimes);
    }

    // long[60] (nanos)
    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly_profiler.get_render_times",
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = {},
                            argumentNames = {},
                            returnType = LuaTable.class
                    )
            }
    )
    public LuaTable getRenderTimes() {
        return getData(lastRenderTimes);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly_profiler.get_world_tick_times",
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = {},
                            argumentNames = {},
                            returnType = LuaTable.class
                    )
            }
    )
    public LuaTable getWorldTickTimes() {
        return getData(lastWorldTickTimes);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly_profiler.get_world_render_times",
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = {},
                            argumentNames = {},
                            returnType = LuaTable.class
                    )
            }
    )
    public LuaTable getWorldRenderTimes() {
        return getData(lastWorldRenderTimes);
    }

}
