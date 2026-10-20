package dev.celestial.silly.not_a_mixin;

import dev.celestial.silly.lua.SillyAPI;
import dev.celestial.silly.lua.SillyProfiler;
import dev.celestial.silly.luaj.LuaOOM;
import dev.celestial.silly.luaj.memory.AllocationTracker;

public interface AvatarExtensions {
    SillyAPI silly$getSilly();
    SillyAPI silly$setSilly(SillyAPI instance);
    Object silly$setUserData(Class<?> clazz, Object instance);
    Object silly$getUserData(Class<?> clazz);

    AllocationTracker<LuaOOM> silly$getAllocationTracker();

    SillyProfiler silly$getProfiler();
    SillyProfiler silly$setProfiler(SillyProfiler instance);
}
