package dev.celestial.sillyplugin.mixin;

import dev.celestial.sillyplugin.lua.SillyAPI;
import org.figuramc.figura.avatar.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = Avatar.class, remap = false)
public class AvatarMixin implements AvatarAccessor {
    @Unique
    public SillyAPI silly;

    @Override
    public SillyAPI silly$getSilly() {
        return silly;
    }

    @Override
    public SillyAPI silly$setSilly(SillyAPI instance) {
        silly = instance;
        return silly;
    }
}
