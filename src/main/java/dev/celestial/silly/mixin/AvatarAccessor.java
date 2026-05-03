package dev.celestial.silly.mixin;

import org.figuramc.figura.avatar.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = Avatar.class, remap = false)
public interface AvatarAccessor {
    @Invoker("getFileSize")
    public int silly$getFileSize();
}
