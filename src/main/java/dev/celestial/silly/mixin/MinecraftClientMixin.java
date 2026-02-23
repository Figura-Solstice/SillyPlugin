package dev.celestial.silly.mixin;

import dev.celestial.silly.SillyPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {
    //? if 1.20.1 {
    @Inject(method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"))
    //?} else {
    /*@Inject(method = "clearClientLevel(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"))
    *///?}
    public void clearLevelMixin(Screen screen, CallbackInfo ci) {
        SillyPlugin.FakeBlocks.clear();
        SillyPlugin.RealBlocks.clear();
    }
}
