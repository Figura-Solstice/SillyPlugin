package dev.celestial.silly.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.celestial.silly.SillyPlugin;
import dev.celestial.silly.lua.BackportsAPI;
import dev.celestial.silly.helper.CallerContext;
import dev.celestial.silly.lua.SillyAPI;
import dev.celestial.silly.not_a_mixin.AvatarAccessor;
import dev.celestial.silly.not_a_mixin.EventsAccessor;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.FiguraLuaRuntime;
import org.figuramc.figura.lua.api.event.LuaEvent;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = FiguraLuaRuntime.class, remap = false)
public abstract class FiguraLuaRuntimeMixin {
    @Unique
    public boolean injectIntoError = true;
    @Shadow
    @Final
    public Avatar owner;

    @Shadow
    public abstract void error(Throwable e);

    @WrapMethod(method = "run")
    public Varargs runMixin(Object toRun, Avatar.Instructions limit, Object[] args, Operation<Varargs> original) {
        if (owner.permissions.get(SillyPlugin.SCRIPT_EXEC) > 1 || toRun == "ENTITY_INIT")
            try(CallerContext ctx = BackportsAPI.openCallerContext(owner.owner, "avatarRun")) {
                return original.call(toRun, limit, args);
            }
        else {
            owner.noPermissions.add(SillyPlugin.SCRIPT_EXEC);
        }
        return null;
    }

    @WrapOperation(method = "init", at = @At(value = "INVOKE", target = "Ljava/util/Map;isEmpty()Z"))
    public boolean initMixin(Map<String, String> instance, Operation<Boolean> original) {
        boolean isEmpty = original.call(instance);
        if (!isEmpty) {
            boolean shouldBeEmptyAnyway = owner.permissions.get(SillyPlugin.SCRIPT_EXEC) == 0;
            if (shouldBeEmptyAnyway) {
                owner.noPermissions.add(SillyPlugin.SCRIPT_EXEC);
                return true;
            }
        }
        return isEmpty;
    }

    @WrapMethod(method = "initializeScript")
    public Varargs initScriptEnter(String str, Operation<Varargs> original) {
        try(CallerContext ctx = BackportsAPI.openCallerContext(owner.owner, "initScript/" + str)) {
            return original.call(str);
        }
    }

    @Inject(method="error", at = @At("HEAD"), cancellable = true)
    public void errorMixin(Throwable e, CallbackInfo ci) {
        if (owner.luaRuntime == null) return;
        LuaEvent ev = ((EventsAccessor)owner.luaRuntime.events).silly$getErrorEvent();
        if (ev.__len() > 0) {
            if (injectIntoError) {
                injectIntoError = false;
                Varargs res = owner.luaRuntime.run("ERROR", owner.tick, e.getMessage());
                if (res == null) return;
                if (res.arg(1).isboolean() && res.arg(1).checkboolean()) {
                    injectIntoError = true;
                    ci.cancel();
                    return;
                } else if (res.arg(1).isstring()) {
                    String val = res.arg(1).checkjstring();
                    ev.clear();
                    ci.cancel();
                    error(new LuaError(val));
                    return;
                }
            } else {
                ev.clear();
                ci.cancel();
                error(new LuaError("Error occurred during error event: " + e.getMessage()));
                return;
            }
        }
        SillyAPI silly = ((AvatarAccessor)owner).silly$getSilly();
        if (silly != null) silly.cleanup();
    }
}
