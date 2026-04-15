package dev.celestial.silly.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.celestial.silly.helper.AutoProfile;
import dev.celestial.silly.lua.SillyAPI;
import dev.celestial.silly.lua.SillyProfiler;
import dev.celestial.silly.not_a_mixin.AvatarAccessor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.Entity;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.model.rendering.PartFilterScheme;
import org.luaj.vm2.Varargs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Avatar.class, remap = false)
public class AvatarMixin implements AvatarAccessor {
    @Unique
    public SillyAPI silly;

    @Unique
    public SillyProfiler profiler;

    @Override
    public SillyAPI silly$getSilly() {
        return silly;
    }

    @Override
    public SillyAPI silly$setSilly(SillyAPI instance) {
        silly = instance;
        return silly;
    }

    @Override
    public SillyProfiler silly$getProfiler() {
        return profiler;
    }

    @Override
    public SillyProfiler silly$setProfiler(SillyProfiler instance) {
        profiler = instance;
        return profiler;
    }

    @WrapMethod(method = "render(F)V")
    public void silly$profileWorldRender(float delta, Operation<Void> original) {
        SillyProfiler prf = ((AvatarAccessor)(Avatar)(Object)this).silly$getProfiler();
        if (prf == null) {
            original.call(delta);
            return;
        }
        try (AutoProfile profiler = AutoProfile.start(prf.lastWorldRenderTimes::push)) {
            original.call(delta);
        }
    }

    @WrapMethod(method = "render(Lnet/minecraft/world/entity/Entity;FFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/renderer/entity/LivingEntityRenderer;Lorg/figuramc/figura/model/rendering/PartFilterScheme;ZZ)V", remap = true)
    //? if >=1.21.4 {
    /*public void silly$profileRender(Entity entity, float yaw, float delta, float alpha, PoseStack stack, MultiBufferSource bufferSource, int light, int overlay, LivingEntityRenderer<?, ?, ?> entityRenderer, PartFilterScheme filter, boolean translucent, boolean glowing, Operation<Void> original) {
    *///?} else {
    public void silly$profileRender(Entity entity, float yaw, float delta, float alpha, PoseStack stack, MultiBufferSource bufferSource, int light, int overlay, LivingEntityRenderer<?, ?> entityRenderer, PartFilterScheme filter, boolean translucent, boolean glowing, Operation<Void> original) {
    //?}
        SillyProfiler prf = this.silly$getProfiler();
        if (prf == null) {
            original.call(entity, yaw, delta, alpha, stack, bufferSource, light, overlay, entityRenderer, filter, translucent, glowing);
            return;
        }
        try (AutoProfile profiler = AutoProfile.start(prf.lastRenderTimes::push)) {
            original.call(entity, yaw, delta, alpha, stack, bufferSource, light, overlay, entityRenderer, filter, translucent, glowing);
        }
    }

    @WrapMethod(method = "tickEvent")
    public void tickWrap(Operation<Void> original) {
        SillyProfiler prf = ((AvatarAccessor)(Avatar)(Object)this).silly$getProfiler();
        if (prf == null) {
            original.call();
            return;
        }
        try (AutoProfile profiler = AutoProfile.start(prf.lastTickEventTimes::push)) {
            original.call();
        }
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lorg/figuramc/figura/avatar/Avatar;run(Ljava/lang/Object;Lorg/figuramc/figura/avatar/Avatar$Instructions;[Ljava/lang/Object;)Lorg/luaj/vm2/Varargs;"))
    public Varargs silly$profileWorldTick(Avatar instance, Object toRun, Avatar.Instructions limit, Object[] args, Operation<Varargs> original) {
        SillyProfiler prf = ((AvatarAccessor)(Avatar)(Object)this).silly$getProfiler();
        if (prf == null) {
            original.call(instance, toRun, limit, args);
            return null;
        }
        try (AutoProfile profiler = AutoProfile.start(prf.lastWorldTickTimes::push)) {
            original.call(instance, toRun, limit, args);
        }
        return null;
    }

    @Inject(method = "clean", at = @At("TAIL"))
    public void cleanMixin(CallbackInfo ci) {
        SillyAPI silly = silly$getSilly();
        if (silly != null) silly.cleanup();
    }
}
