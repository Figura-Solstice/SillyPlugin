package dev.celestial.silly.mixin;

import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.celestial.silly.lua.BackportsAPI;
import dev.celestial.silly.lua.SillyAPI;
import dev.celestial.silly.not_a_mixin.AvatarAccessor;
import dev.celestial.silly.not_a_mixin.EventsAccessor;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.FiguraLuaRuntime;
import org.figuramc.figura.lua.api.event.LuaEvent;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.Varargs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(value = FiguraLuaRuntime.class, remap = false)
public abstract class FiguraLuaRuntimeMixin {
    @Unique
    public boolean injectIntoError = true;
    @Shadow
    @Final
    public Avatar owner;

    @Shadow
    public abstract void error(Throwable e);

    @Inject(method = "run", at = @At(value = "HEAD"))
    public void runEnter(Object toRun, Avatar.Instructions limit, Object[] args, CallbackInfoReturnable<Varargs> cir) {
        BackportsAPI.pushStack(owner.owner, "avatar.run/" + toRun.toString());
    }

    @Inject(method = "run", at = @At("RETURN"))
    public void runExit(Object toRun, Avatar.Instructions limit, Object[] args, CallbackInfoReturnable<Varargs> cir) {
        BackportsAPI.popStack(owner.owner, "avatar.run/" + toRun.toString());
    }

    @Inject(method = "initializeScript", at = @At(value = "INVOKE", target = "Ljava/util/Stack;push(Ljava/lang/Object;)Ljava/lang/Object;"))
    public void initScriptEnter(String str, CallbackInfoReturnable<Varargs> cir) {
        BackportsAPI.pushStack(owner.owner, "initScript/" + str);
    }

    @Inject(method = "initializeScript", at = @At("TAIL"))
    public void initScriptExit(String str, CallbackInfoReturnable<Varargs> cir) {
        BackportsAPI.popStack(owner.owner, "initScript/" + str);
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
