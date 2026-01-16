package dev.celestial.silly.mixin;

import dev.celestial.silly.lua.SillyAPI;
import dev.celestial.silly.not_a_mixin.AvatarAccessor;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.FiguraLuaRuntime;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FiguraLuaRuntime.class, remap = false)
public class FiguraLuaRuntimeMixin {
    @Shadow
    @Final
    public Avatar owner;

    @Inject(method="error", at = @At("HEAD"))
    public void errorMixin(Throwable e, CallbackInfo ci) {
        SillyAPI silly = ((AvatarAccessor)owner).silly$getSilly();
        if (silly != null) silly.cleanup();
    }
}
