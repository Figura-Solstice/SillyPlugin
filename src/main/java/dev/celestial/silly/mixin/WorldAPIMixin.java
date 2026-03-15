package dev.celestial.silly.mixin;

import com.llamalad7.mixinextras.lib.apache.commons.tuple.Pair;
import dev.celestial.silly.helper.CallerContext;
import dev.celestial.silly.lua.BackportsAPI;
import org.figuramc.figura.lua.ReadOnlyLuaTable;
import org.figuramc.figura.lua.api.world.WorldAPI;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.VarArgFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(value = WorldAPI.class, remap = false)
public class WorldAPIMixin {
    @Inject(method = "avatarVars", at = @At(value = "RETURN"), cancellable = true)
    private static void avatarVarsMixin(CallbackInfoReturnable<Map<String, LuaTable>> cir) {
        Map<String, LuaTable> val = cir.getReturnValue();
        Map<String, LuaTable> ret = new HashMap<>();
        Pair<UUID, String> caller = BackportsAPI.callerStack.get().peek();
        if (caller == null) throw new IllegalStateException("Caller stack peek gave null (?!?!?!?)");
        for (Map.Entry<String, LuaTable> e : val.entrySet()) {
            ret.put(e.getKey(), createReadOnlyLuaTable(silly$transformTable(e.getValue(), caller.getLeft())));
        }
        cir.setReturnValue(ret);
    }

    @Unique
    private static Class<?> roltClass;
    @Unique
    private static LuaTable createReadOnlyLuaTable(LuaTable tbl) {
        if (roltClass != null) {
            try {
                return (LuaTable) roltClass.getConstructor(LuaValue.class).newInstance(tbl);
            } catch (Exception e) {
                throw new LuaError(e);
            }
        }
        try {
            roltClass = Class.forName("org.figuramc.figura.lua.ReadOnlyLuaTable");
        } catch (Exception e) {
            try {
                roltClass = Class.forName("org.figuramc.figura.lua.transfer.ReadOnlyLuaTable");
            } catch (ClassNotFoundException ex) {
                throw new LuaError("Could not create ReadOnlyLuaTable!");
            }
        }
        try {
            return (LuaTable) roltClass.getConstructor(LuaValue.class).newInstance(tbl);
        } catch (Exception e) {
            throw new LuaError(e);
        }
    }

    @Unique
    private static LuaTable silly$transformTable(LuaTable table, UUID caller) {
        LuaTable ret = new LuaTable();

        for (LuaValue key : table.keys()) {
            LuaValue value = table.rawget(key);
            if (value.isfunction()) {
                ret.rawset(key, silly$transformFunction(value.checkfunction(), caller));
            } else if (value.istable()) {
                ret.rawset(key, silly$transformTable(value.checktable(), caller));
            } else {
                ret.rawset(key, value);
            }
        }
        return ret;
    }

    @Unique
    private static LuaValue silly$transformFunction(LuaFunction func, UUID caller) {
        return new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                try(CallerContext ctx = BackportsAPI.openCallerContext(caller, "TransformedFunction/" + func.hashCode())) {
                    return func.invoke(args);
                }
            }
        };
    }
}
