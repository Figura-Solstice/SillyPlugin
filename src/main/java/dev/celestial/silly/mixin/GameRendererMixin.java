package dev.celestial.silly.mixin;

import dev.celestial.silly.lua.SillyAPI;
import dev.celestial.silly.not_a_mixin.AvatarExtensions;
//? if >1.21 {
/*import net.minecraft.client.DeltaTracker;
*///?}

import net.minecraft.client.renderer.GameRenderer;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "render", at = @At("HEAD"))
    //? if >1.21 {
    /*private void silly$prerenderEvent(DeltaTracker deltaTracker, boolean bl, CallbackInfo ci) {
    *///?} else {
    private void silly$prerenderEvent(float f, long l, boolean bl, CallbackInfo ci) {
    //?}
        if (AvatarManager.panic) return;
        AvatarManager.executeAll("preRenderBackport", a -> {
            SillyAPI silly = ((AvatarExtensions)a).silly$getSilly();
            if (silly == null) return;
            //? if >1.21 {
            /*a.run(silly.backports.PRE_RENDER, a.render, deltaTracker.getGameTimeDeltaPartialTick(bl), a.renderMode.name());
            *///?} else {
            a.run(silly.backports.PRE_RENDER, a.render, f, a.renderMode.name());
            //?}

        });
    }
}
