package dev.celestial.silly.mixin;

import dev.celestial.silly.SillyPlugin;
import dev.celestial.silly.lua.SillyAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Abilities;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(value = AvatarManager.class, remap = false)
public abstract class AvatarManagerMixin {
    @Inject(method="reloadAvatar", at = @At("HEAD"))
    private static void reloadAvatarMixin(UUID id, CallbackInfo ci) {
        Avatar av = AvatarManager.getLoadedAvatar(id);
        if (av != null)
            ((AvatarAccessor)av).silly$getSilly().cleanup();
    }
}
