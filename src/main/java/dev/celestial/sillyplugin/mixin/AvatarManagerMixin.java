package dev.celestial.sillyplugin.mixin;

import dev.celestial.sillyplugin.SillyPlugin;
import dev.celestial.sillyplugin.client.SillyPluginClient;
import dev.celestial.sillyplugin.lua.SillyAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Abilities;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.AvatarManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(value = AvatarManager.class, remap = false)
public class AvatarManagerMixin {
    @Inject(method="reloadAvatar", at = @At("HEAD"))
    private static void reloadAvatarMixin(UUID id, CallbackInfo ci) {
        if (SillyPluginClient.hostInstance != null)
            SillyPluginClient.hostInstance.cleanup();
    }
}
