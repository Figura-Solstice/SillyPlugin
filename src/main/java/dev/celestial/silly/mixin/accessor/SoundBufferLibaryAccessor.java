package dev.celestial.silly.mixin.accessor;

import com.mojang.blaze3d.audio.SoundBuffer;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Mixin(SoundBufferLibrary.class)
public interface SoundBufferLibaryAccessor {
    @Accessor("cache")
    Map<ResourceLocation, CompletableFuture<SoundBuffer>> getCache();
}
