package dev.celestial.silly.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import dev.celestial.silly.SillyPlugin;
import dev.celestial.silly.lua.SillyAPI;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.figuramc.figura.avatar.AvatarManager;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends Player {
    public LocalPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    //? if neoforge && >=1.21 {
    /*@WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;mayFly()Z"))
    public boolean mayFlyMixin(LocalPlayer instance, Operation<Boolean> original) {
        boolean orig = original.call(instance);
        SillyAPI silly = SillyPlugin.hostInstance;
        if (silly == null) return orig;
        if (!silly.mayFlyOverride) return orig;
        if (!silly.cheatsEnabled()) return orig;
        if (AvatarManager.panic) return orig;
        return silly.mayFly;
    }
    *///?} else {
    @Redirect(method = "aiStep", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Abilities;mayfly:Z", opcode = Opcodes.GETFIELD))
    public boolean getAbilitiesMixin(Abilities instance) {
        SillyAPI silly = SillyPlugin.hostInstance;
        if (silly == null) return instance.mayfly;
        if (!silly.mayFlyOverride) return instance.mayfly;
        if (!silly.cheatsEnabled()) return instance.mayfly;
        if (AvatarManager.panic) return instance.mayfly;
        return silly.mayFly;
    }
    //?}
}
