package dev.celestial.silly.mixin.events;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.audio.SoundBuffer;
import dev.celestial.silly.lua.data.SillySoundEventData;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.api.sound.LuaSound;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;

@Mixin(LuaSound.class)
public class LuaSoundMixin {
    @Shadow
    @Final
    private SoundBuffer buffer;

    @Shadow
    private Component subtitleText;

    @Shadow
    @Final
    private Avatar owner;

    @WrapOperation(method = "lambda$play$0", at = @At(value = "INVOKE", target = "Lorg/figuramc/figura/avatar/Avatar;playSoundEvent(Ljava/lang/String;Lorg/figuramc/figura/math/vector/FiguraVec3;FFZLjava/lang/String;Ljava/lang/String;)Z"))
    public boolean silly$onPlaySoundButCooler(Avatar instance, String id, FiguraVec3 pos, float vol, float pitch, boolean loop, String category, String file, Operation<Boolean> original) {
        var data = new SillySoundEventData();
        data.pos = pos;
        data.volume = vol;
        data.pitch = pitch;
        data.looping = loop;
        data.file = file;
        data.category = category;
        data.id = id;
        data.owner = owner.owner.toString();
        if (this.buffer != null)
            data.fillFromSoundBuffer(this.buffer);
        else
            data.channels = -1;
        data.subtitle = subtitleText;

        instance.run("SILLY_SOUND_EVENT", instance.tick, data);
        return original.call(instance, id, pos, vol, pitch, loop, category, file) || data.cancelled;
    }

    @WrapOperation(method = "lambda$play$0", at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z"))
    public <E> boolean silly$fixNoPermissions(Set<E> instance, E e, Operation<Boolean> original, @Local(name = "cancel") boolean cancel) {
        if (cancel)
            return original.call(instance, e);
        return false;
    }
}
