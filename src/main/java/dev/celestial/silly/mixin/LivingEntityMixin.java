package dev.celestial.silly.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import dev.celestial.silly.SillyPlugin;
import dev.celestial.silly.lua.SillyAPI;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.figuramc.figura.avatar.AvatarManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    //? if <=1.21.1 {
    /*@Inject(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"))
    public void silly$gravity(Vec3 vec3, CallbackInfo ci, @Local(ordinal = 0) LocalDoubleRef d) {
        if ((LivingEntity)(Object)this instanceof LocalPlayer) {
            SillyAPI silly = SillyPlugin.hostInstance;
            if (silly == null) return;
            if (!silly.gravity.isOverridden()) return;
            if (!silly.cheatsEnabled()) return;
            if (AvatarManager.panic) return;
            d.set(silly.gravity.getValue() * d.get());
        }
    }
    @WrapOperation(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;onGround()Z", ordinal = 2))
    public boolean silly$frictionInAir(LivingEntity instance, Operation<Boolean> original) {
        var orig = original.call(instance);
        if ((LivingEntity)(Object)this instanceof LocalPlayer) {
            SillyAPI silly = SillyPlugin.hostInstance;
            if (silly == null) return orig;
            if (!silly.frictionInAir.isOverridden()) return orig;
            if (!silly.cheatsEnabled()) return orig;
            if (AvatarManager.panic) return orig;
            return silly.frictionInAir.getValue();
        } else {
            return orig;
        }
    }
    *///?} else {
    @Inject(method = "getEffectiveGravity", at = @At(value = "RETURN"), cancellable = true)
    public void silly$gravity(CallbackInfoReturnable<Double> cir) {
        if ((LivingEntity)(Object)this instanceof LocalPlayer) {
            SillyAPI silly = SillyPlugin.hostInstance;
            if (silly == null) return;
            if (!silly.gravity.isOverridden()) return;
            if (!silly.cheatsEnabled()) return;
            if (AvatarManager.panic) return;
            cir.setReturnValue(silly.gravity.getValue() * cir.getReturnValue());
        }
    }
    @WrapOperation(method = "travelInAir", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;onGround()Z", ordinal = 0))
    public boolean silly$frictionInAir(LivingEntity instance, Operation<Boolean> original) {
        var orig = original.call(instance);
        if ((LivingEntity)(Object)this instanceof LocalPlayer) {
            SillyAPI silly = SillyPlugin.hostInstance;
            if (silly == null) return orig;
            if (!silly.frictionInAir.isOverridden()) return orig;
            if (!silly.cheatsEnabled()) return orig;
            if (AvatarManager.panic) return orig;
            return silly.frictionInAir.getValue();
        } else {
            return orig;
        }
    }
    //?}
}
