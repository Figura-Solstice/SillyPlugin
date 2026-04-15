package dev.celestial.silly.not_a_mixin;

import dev.celestial.silly.lua.SillyAPI;
import dev.celestial.silly.lua.SillyProfiler;

public interface AvatarAccessor {
    SillyAPI silly$getSilly();
    SillyAPI silly$setSilly(SillyAPI instance);

    SillyProfiler silly$getProfiler();
    SillyProfiler silly$setProfiler(SillyProfiler instance);
}
