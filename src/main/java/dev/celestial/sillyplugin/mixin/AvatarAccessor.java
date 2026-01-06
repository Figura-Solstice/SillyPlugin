package dev.celestial.sillyplugin.mixin;

import dev.celestial.sillyplugin.lua.SillyAPI;

public interface AvatarAccessor {
    SillyAPI silly$getSilly();
    SillyAPI silly$setSilly(SillyAPI instance);
}
