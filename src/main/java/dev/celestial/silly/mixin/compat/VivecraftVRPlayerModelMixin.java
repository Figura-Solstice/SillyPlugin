package dev.celestial.silly.mixin.compat;

import dev.celestial.silly.lua.compat.VivecraftCompatAPI;
import net.minecraft.client.Minecraft;
//? if >=1.21.4 {
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
//?}
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(value = {
        org.vivecraft.client.render.VRPlayerModel_WithArms.class,
        org.vivecraft.client.render.VRPlayerModel_WithArmsLegs.class
}, remap = false)
public class VivecraftVRPlayerModelMixin {

    //? if >=1.21.4 {
    @Inject(
            method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/vivecraft/client/render/VRPlayerModel;setupAnim(Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;)V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void silly$disableVivecraftAnim(PlayerRenderState state, CallbackInfo ci) {
        if (Minecraft.getInstance().level == null) return;
        Entity entity = Minecraft.getInstance().level.getEntity(state.id);
        if (!(entity instanceof LivingEntity living)) return;
        if (!VivecraftCompatAPI.shouldDoVivecraftAnim(living.getUUID())) {
            ci.cancel();
        }
    }
    //?} else {
    /*@Inject(
            method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/vivecraft/client/render/VRPlayerModel;setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private <T extends LivingEntity> void silly$disableVivecraftAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!VivecraftCompatAPI.shouldDoVivecraftAnim(entity.getUUID())) {
            ci.cancel();
        }
    }
    *///?}
}
