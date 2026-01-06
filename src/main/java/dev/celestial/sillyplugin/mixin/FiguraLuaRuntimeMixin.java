package dev.celestial.sillyplugin.mixin;

import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.FiguraLuaRuntime;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FiguraLuaRuntime.class)
public class FiguraLuaRuntimeMixin {
    @Shadow
    @Final
    public Avatar owner;

    @Inject(method="error", at = @At("HEAD"))
    public void errorMixin(Throwable e, CallbackInfo ci) {
        ((AvatarAccessor)owner).silly$getSilly().cleanup();
    }
}
