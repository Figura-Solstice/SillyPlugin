package dev.celestial.silly.mixin.events;

import com.mojang.blaze3d.audio.Listener;
import dev.celestial.silly.lua.data.SillySoundEventData;
import dev.celestial.silly.mixin.accessor.SoundBufferLibaryAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.world.phys.Vec3;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.permissions.Permissions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// org/figuramc/figura/mixin/sound/SoundEngineMixin.java
@Mixin(SoundEngine.class)
public class SoundEngineMixin {
    @Shadow
    @Final
    private Listener listener;

    @Shadow
    @Final
    private SoundBufferLibrary soundBuffers;

    // targeting getGain is more likely to target more versions of Minecraft, but targeting the `listeners` list blocks subtitles. I believe blocking subtitles was more important.
    // If we end up targeting a version of minecraft with no subtitles, use getGain. In vanilla, `listeners` is only used for subtitles and may not be in versions without subtitles.
    //@Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/audio/Listener;getGain()Z"), method = "play", cancellable = true)
    @Inject(at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z"), method = "play", cancellable = true)
    public void play(SoundInstance sound, CallbackInfo c) {
        // "Can hear sound" check stolen from 381 of SoundEngine
        float g = Math.max(sound.getVolume(), 1.0F) * (float)sound.getSound().getAttenuationDistance();
        Vec3 pos = new Vec3(sound.getX(), sound.getY(), sound.getZ());
        //? if >1.20.1 {
        if (sound.isRelative() || sound.getAttenuation() == SoundInstance.Attenuation.NONE || this.listener.getTransform().position().distanceToSqr(pos) < (double)(g * g)){
        //?} else {
        /*if (sound.isRelative() || sound.getAttenuation() == SoundInstance.Attenuation.NONE || this.listener.getListenerPosition().distanceToSqr(pos) < (double)(g * g)){
        *///?}
            // Run sound event
            var data = new SillySoundEventData();
            data.pos = FiguraVec3.fromVec3(pos);
            data.volume = sound.getVolume();
            data.pitch = sound.getPitch();
            data.looping = sound.isLooping();
            data.file = sound.getSound().getLocation().toString();
            data.category = sound.getSource().getName();
            data.id = sound.getLocation().toString();
            var cache = ((SoundBufferLibaryAccessor)soundBuffers).getCache();
            var fut = cache.get(sound.getLocation());
            if (fut != null && fut.isDone()) {
                try {
                    data.fillFromSoundBuffer(fut.get());
                } catch (Exception ignored) {
                    // this should never happen.
                    data.channels = -1;
                }
            } else {
                data.channels = -1;
            }
            WeighedSoundEvents events = Minecraft.getInstance().getSoundManager().getSoundEvent(sound.getLocation());
            if (events != null) {
                data.subtitle = events.getSubtitle();
            }

            AvatarManager.executeAll("playSoundEvent", avatar -> {
                avatar.run("SILLY_SOUND_EVENT", avatar.tick, data);
                if (avatar.permissions.get(Permissions.CANCEL_SOUNDS) >= 1) {
                    avatar.noPermissions.remove(Permissions.CANCEL_SOUNDS);
                    if (data.cancelled) {
                        c.cancel(); // calling cancel multiple times is fine, right?
                    }
                } else if (data.cancelled) {
                    avatar.noPermissions.add(Permissions.CANCEL_SOUNDS);
                }
            });
        }
    }
}
