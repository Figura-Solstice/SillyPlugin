package dev.celestial.silly.mixin.hud;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.celestial.silly.SillyEnums;
import dev.celestial.silly.SillyPlugin;
//? if >=1.21 {
/*import net.minecraft.client.DeltaTracker;
*///?}
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    @Unique
    private static void silly$cancelIfHidden(SillyEnums.GUI_ELEMENT el, CallbackInfo ci) {
        if (SillyPlugin.shouldHide(el)) ci.cancel();
    }

    //? if >=1.21 {
    /*@Inject(method="renderHotbarAndDecorations", at = @At("HEAD"), cancellable = true)
    public void renderHotbarMixin(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) { 
    *///?} else if >=1.20.5 {
    /*@Inject(method="renderHotbarAndDecorations", at = @At("HEAD"), cancellable = true)
    public void renderHotbarMixin(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) { */
    //?} else {
    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    public void renderHotbarMixin(float f, GuiGraphics guiGraphics, CallbackInfo ci) {
    //?}
        silly$cancelIfHidden(SillyEnums.GUI_ELEMENT.HOTBAR, ci);
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    public void renderExperienceBarMixin(GuiGraphics guiGraphics, int i, CallbackInfo ci) {
        silly$cancelIfHidden(SillyEnums.GUI_ELEMENT.EXPERIENCE_BAR, ci);
    }

    @Inject(method = "renderJumpMeter", at = @At("HEAD"), cancellable = true)
    public void renderJumpMeterMixin(PlayerRideableJumping playerRideableJumping, GuiGraphics guiGraphics, int i, CallbackInfo ci) {
        silly$cancelIfHidden(SillyEnums.GUI_ELEMENT.JUMP_METER, ci);
    }

    @Inject(method = "renderSelectedItemName", at = @At("HEAD"), cancellable = true)
    public void renderSelectedItemNameMixin(GuiGraphics guiGraphics, CallbackInfo ci) {
        silly$cancelIfHidden(SillyEnums.GUI_ELEMENT.SELECTED_ITEM_NAME, ci);
    }

    @Inject(method = "renderHearts", at = @At("HEAD"), cancellable = true)
    public void renderPlayerHealthMixin(GuiGraphics guiGraphics, Player player, int i, int j, int k, int l, float f, int m, int n, int o, boolean bl, CallbackInfo ci) {
        silly$cancelIfHidden(SillyEnums.GUI_ELEMENT.PLAYER_HEALTH, ci);
    }
    //? if neoforge && >=1.21 {
    /*@Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private static void renderArmorMixin(GuiGraphics p_335393_, Player p_335672_, int p_335452_, int p_335846_, int p_335778_, int p_335859_, CallbackInfo ci) {
        silly$cancelIfHidden(SillyEnums.GUI_ELEMENT.ARMOR_BAR, ci);
    }
    *///?} else if >=1.21 {
    /*@Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private static void renderArmorMixin(GuiGraphics p_335393_, Player p_335672_, int p_335452_, int p_335846_, int p_335778_, int p_335859_, CallbackInfo ci) {
        silly$cancelIfHidden(SillyEnums.GUI_ELEMENT.ARMOR_BAR, ci);
    }
    *///?} else {
    @WrapOperation(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getArmorValue()I"))
    public int renderArmorMixin(Player instance, Operation<Integer> original) {
        return SillyPlugin.shouldHide(SillyEnums.GUI_ELEMENT.ARMOR_BAR) ? 0 : original.call(instance);
    }
     //?}

    //? if neoforge && >=1.21 {
    /*@WrapOperation(method = "renderFoodLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;getVehicleMaxHearts(Lnet/minecraft/world/entity/LivingEntity;)I"))
    *///?} else {
    @WrapOperation(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;getVehicleMaxHearts(Lnet/minecraft/world/entity/LivingEntity;)I"))
    //?}
    public int renderPlayerHealthMixin(Gui instance, LivingEntity i, Operation<Integer> original) {
        if (SillyPlugin.shouldHide(SillyEnums.GUI_ELEMENT.PLAYER_HUNGER)) return 1;
        return original.call(instance, i);
    }

    @Inject(method = "renderVehicleHealth", at = @At("HEAD"), cancellable = true)
    public void renderVehicleHealthMixin(GuiGraphics guiGraphics, CallbackInfo ci) {
        silly$cancelIfHidden(SillyEnums.GUI_ELEMENT.VEHICLE_HEALTH, ci);
    }

    @Inject(method = "renderVignette", at = @At("HEAD"), cancellable = true)
    public void renderVignetteMixin(GuiGraphics guiGraphics, Entity entity, CallbackInfo ci) {
        silly$cancelIfHidden(SillyEnums.GUI_ELEMENT.VIGNETTE, ci);
    }

    @Inject(method = "renderSpyglassOverlay", at = @At("HEAD"), cancellable = true)
    public void renderSpyglassOverlayMixin(GuiGraphics guiGraphics, float f, CallbackInfo ci) {
        silly$cancelIfHidden(SillyEnums.GUI_ELEMENT.SPYGLASS_OVERLAY, ci);
    }

    @Inject(method = "renderTextureOverlay", at = @At("HEAD"), cancellable = true)
    public void renderTextureOverlayMixin(GuiGraphics guiGraphics, ResourceLocation resourceLocation, float f, CallbackInfo ci) {
        silly$cancelIfHidden(SillyEnums.GUI_ELEMENT.TEXTURE_OVERLAY, ci);
    }

    @Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
    public void renderPortalOverlayMixin(GuiGraphics guiGraphics, float f, CallbackInfo ci) {
        silly$cancelIfHidden(SillyEnums.GUI_ELEMENT.PORTAL_OVERLAY, ci);
    }



    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
    //? if >=1.21 {
    /*public void renderEffectsMixin(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
    *///?} else {
    public void renderEffectsMixin(GuiGraphics guiGraphics, CallbackInfo ci) {
    //?}
        silly$cancelIfHidden(SillyEnums.GUI_ELEMENT.EFFECTS, ci);
    }
}
