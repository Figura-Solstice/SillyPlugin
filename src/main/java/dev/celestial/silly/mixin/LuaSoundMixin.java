package dev.celestial.silly.mixin;

import com.mojang.blaze3d.audio.SoundBuffer;
import dev.celestial.silly.SillyPermissions;
import dev.celestial.silly.SillySettings;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.ChannelAccess;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.gui.FiguraToast;
import org.figuramc.figura.lua.api.sound.LuaSound;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LuaSound.class, remap = false)
public class LuaSoundMixin {
    @Shadow
    private ChannelAccess.ChannelHandle handle;

    @Shadow
    @Final
    private Avatar owner;

    @Shadow
    @Final
    private SoundBuffer buffer;

    @Unique
    private boolean silly$shouldCancelStereoSound() {
        if (owner.permissions.get(SillyPermissions.STEREO_SOUNDS) == 0) {
            owner.noPermissions.add(SillyPermissions.STEREO_SOUNDS);
            return true;
        } else {
            owner.noPermissions.remove(SillyPermissions.STEREO_SOUNDS);
            if (SillySettings.TOAST_ON_STEREO.getBool() && !owner.isHost)
                FiguraToast.sendToast("Stereo sound", "By " + owner.entityName);
        }
        return false;
    }

    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    public void silly$stereoPermission(CallbackInfoReturnable<LuaSound> cir) {
        var buf = this.buffer;
        if (buf != null) {
            var format = ((SoundBufferAccessor)buf).silly$getFormat();
            if (format.getChannels() > 1) {
                if (silly$shouldCancelStereoSound())
                    cir.setReturnValue((LuaSound)(Object)this);
            }
        }
    }

    @Inject(method = "lambda$play$4", at = @At("HEAD"), cancellable = true)
    public void silly$stereoPermissionVanillaSoundNormal(SoundBuffer buffer, CallbackInfo ci) {
        var format = ((SoundBufferAccessor)buffer).silly$getFormat();
        if (format.getChannels() > 1) {
            if (silly$shouldCancelStereoSound())
                ci.cancel();
        }
    }

    @Inject(method = "lambda$play$6", at = @At("HEAD"), cancellable = true)
    public void silly$stereoPermissionVanillaSoundStream(AudioStream stream, CallbackInfo ci) {
        var format = stream.getFormat();
        if (format.getChannels() > 1) {
            if (silly$shouldCancelStereoSound())
                ci.cancel();
        }
    }
}
