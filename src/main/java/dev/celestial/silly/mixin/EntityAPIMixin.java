package dev.celestial.silly.mixin;

import com.llamalad7.mixinextras.lib.apache.commons.tuple.Pair;
import dev.celestial.silly.SillyUtil;
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
            cir.setReturnValue(SillyUtil.transformTableWithCaller(value.checktable(), caller.getLeft(), entityUUID));
        else if (value.isfunction())
            cir.setReturnValue(SillyUtil.transformFunctionWithCaller(value.checkfunction(), caller.getLeft(), entityUUID));
    }
}
