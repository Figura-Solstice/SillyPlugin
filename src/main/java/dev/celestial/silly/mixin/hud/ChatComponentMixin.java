package dev.celestial.silly.mixin.hud;

import dev.celestial.silly.SillyEnums;
import dev.celestial.silly.SillyPlugin;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    //? if >=1.21 {
    /*public void renderMixin(GuiGraphics guiGraphics, int i, int j, int k, boolean bl, CallbackInfo ci) {
    *///?} else {
    public void renderMixin(GuiGraphics guiGraphics, int i, int j, int k, CallbackInfo ci) {
     //?}
        if (SillyPlugin.shouldHide(SillyEnums.GUI_ELEMENT.CHAT)) ci.cancel();
    }
}
