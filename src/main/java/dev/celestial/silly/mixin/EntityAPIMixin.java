package dev.celestial.silly.mixin;

import com.llamalad7.mixinextras.lib.apache.commons.tuple.Pair;
import dev.celestial.silly.lua.BackportsAPI;
import dev.celestial.silly.helper.CallerContext;
import org.figuramc.figura.lua.api.entity.EntityAPI;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(value = EntityAPI.class, remap = false)
public class EntityAPIMixin {
    @Shadow
    @Final
    @NotNull
    protected UUID entityUUID;

    @Inject(method = "getVariable", at = @At(value = "RETURN"), cancellable = true)
    public void getVariableMixin(String key, CallbackInfoReturnable<LuaValue> cir) {
        LuaValue value = cir.getReturnValue();
        Pair<UUID, String> caller = BackportsAPI.callerStack.get().peek();
        if (caller == null) throw new IllegalStateException("Caller stack peek gave null (?!?!?!?)");
        if (value.istable())
            cir.setReturnValue(silly$transformTable(value.checktable(), caller.getLeft(), entityUUID));
        else if (value.isfunction())
            cir.setReturnValue(silly$transformFunction(value.checkfunction(), caller.getLeft(), entityUUID));
    }

    @Unique
    private static LuaTable silly$transformTable(LuaTable table, UUID caller, UUID owner) {
        LuaTable ret = new LuaTable();

        for (LuaValue key : table.keys()) {
            LuaValue value = table.rawget(key);
            if (value.isfunction()) {
                ret.rawset(key, silly$transformFunction(value.checkfunction(), caller, owner));
            } else if (value.istable()) {
                ret.rawset(key, silly$transformTable(value.checktable(), caller, owner));
            } else {
                ret.rawset(key, value);
            }
        }
        return ret;
    }

    @Unique
    private static LuaValue silly$transformFunction(LuaFunction func, UUID caller, UUID owner) {
        return new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                try(CallerContext ctx = BackportsAPI.openCallerContext(caller, owner, "TransformedFunction/" + func.hashCode())) {
                    return func.invoke(args);
                }
            }
        };
    }
}
