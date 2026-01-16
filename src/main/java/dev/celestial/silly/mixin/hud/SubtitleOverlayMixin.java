package dev.celestial.silly.mixin.hud;

import dev.celestial.silly.SillyEnums;
import dev.celestial.silly.SillyPlugin;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.SubtitleOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SubtitleOverlay.class)
public class SubtitleOverlayMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void renderMixin(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (SillyPlugin.shouldHide(SillyEnums.GUI_ELEMENT.SUBTITLE_OVERLAY)) ci.cancel();
    }
}
