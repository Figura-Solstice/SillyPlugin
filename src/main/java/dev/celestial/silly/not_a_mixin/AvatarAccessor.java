package dev.celestial.silly.not_a_mixin;

import dev.celestial.silly.lua.SillyAPI;

public interface AvatarAccessor {
    SillyAPI silly$getSilly();
    SillyAPI silly$setSilly(SillyAPI instance);
}
