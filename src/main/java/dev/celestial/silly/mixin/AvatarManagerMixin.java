package dev.celestial.silly.mixin;

import dev.celestial.silly.lua.SillyAPI;
import dev.celestial.silly.not_a_mixin.AvatarAccessor;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(value = AvatarManager.class, remap = false)
public abstract class AvatarManagerMixin {
    @Inject(method="reloadAvatar", at = @At("HEAD"))
    private static void reloadAvatarMixin(UUID id, CallbackInfo ci) {
        Avatar av = AvatarManager.getLoadedAvatar(id);
        if (av != null) {
            SillyAPI silly = ((AvatarAccessor)av).silly$getSilly();
            if (silly != null) silly.cleanup();
        }
    }
}
