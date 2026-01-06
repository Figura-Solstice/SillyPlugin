package dev.celestial.silly.mixin;

import dev.celestial.silly.lua.SillyAPI;

public interface AvatarAccessor {
    SillyAPI silly$getSilly();
    SillyAPI silly$setSilly(SillyAPI instance);
}
