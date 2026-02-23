package dev.celestial.silly.mixin;

import dev.celestial.silly.SillyPlugin;
import dev.celestial.silly.lua.SillyAPI;
import dev.celestial.silly.not_a_mixin.AvatarAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Dictionary;
import java.util.UUID;

@Mixin(value = AvatarManager.class, remap = false)
public abstract class AvatarManagerMixin {
    @Shadow
    public static boolean panic;

    @Inject(method="reloadAvatar", at = @At("HEAD"))
    private static void reloadAvatarMixin(UUID id, CallbackInfo ci) {
        Avatar av = AvatarManager.getLoadedAvatar(id);
        if (av != null) {
            SillyAPI silly = ((AvatarAccessor)av).silly$getSilly();
            if (silly != null) silly.cleanup();
        }
    }

    @Inject(method = "togglePanic", at = @At("TAIL"))
    private static void togglePanicMixin(CallbackInfo ci) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        SillyPlugin.flattenedFakes().forEach((pos, state) -> {
            if (panic) {
                var real = SillyPlugin.RealBlocks.get(pos);
                if (real != null) {
                    level.setBlock(pos, real.getLeft(), 2);
                    if (real.getRight() != null)
                        level.setBlockEntity(real.getRight());
                }
            } else {
                level.setBlock(pos, state, 2);;
            }
        });
    }
}
