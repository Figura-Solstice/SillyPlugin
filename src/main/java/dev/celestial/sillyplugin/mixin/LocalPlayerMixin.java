package dev.celestial.sillyplugin.mixin;

import com.mojang.authlib.GameProfile;
import dev.celestial.sillyplugin.client.SillyPluginClient;
import dev.celestial.sillyplugin.lua.SillyAPI;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.figuramc.figura.avatar.AvatarManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends Player {
    public LocalPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAbilities()Lnet/minecraft/world/entity/player/Abilities;", ordinal = 1))
    public Abilities getAbilitiesMixin(LocalPlayer instance) {
        Abilities orig = this.getAbilities();
        SillyAPI silly = SillyPluginClient.hostInstance;
        if (silly == null) return orig;
        if (!silly.mayFlyOverride) return orig;
        if (AvatarManager.panic) return orig;
        Abilities newA = new Abilities();
        newA.mayfly = silly.mayFly;
        newA.flying = orig.flying;
        newA.instabuild = orig.instabuild;
        newA.invulnerable = orig.invulnerable;
        newA.mayBuild = orig.mayBuild;
        newA.setFlyingSpeed(orig.getFlyingSpeed());
        newA.setWalkingSpeed(orig.getWalkingSpeed());
        return newA;
    }
}
