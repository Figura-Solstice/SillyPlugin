package dev.celestial.silly.lua;

import com.llamalad7.mixinextras.lib.apache.commons.tuple.Pair;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.FiguraLuaRuntime;
import org.figuramc.figura.lua.LuaWhitelist;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.util.UUID;

// lua functions to aid in debugging SillyPlugin.
// enable by going to SillyUtil and setting DEV_MODE to true.
@LuaWhitelist
public class DevAPI {
    public Avatar owner;
    public FiguraLuaRuntime runtime;
    public static boolean throw_on_bad_call_stack = true;
    public DevAPI(FiguraLuaRuntime runtime) {
        this.runtime = runtime;
        this.owner = runtime.owner;
    }

    // ...maybe not super useful, since it'd (theoretically) only run
    // while its your turn on the stack
    @LuaWhitelist
    public LuaValue get_caller_stack() {
        LuaTable ret = new LuaTable();
        int index = 1;
        for (Pair<UUID, String> stackValue : BackportsAPI.callerStack.get()) {
            LuaTable value = new LuaTable();
            value.set(1, stackValue.getLeft().toString());
            value.set(2, stackValue.getRight());
            ret.set(index, value);
            index++;
        }

        return ret;
    }

    @LuaWhitelist
    public LuaTable get_stack_ops() {
        LuaTable ret = new LuaTable();
        int i = 1;
        for (String op : BackportsAPI.ops.get()) {
            ret.set(i, op);
            i++;
        }
        return ret;
    }

    @LuaWhitelist
    public void disable_throw_on_bad_call_stack() {
        throw_on_bad_call_stack = false;
    }
}
