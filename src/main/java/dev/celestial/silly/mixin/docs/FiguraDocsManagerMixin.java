package dev.celestial.silly.mixin.docs;

import dev.celestial.silly.SillyEnums;
import dev.celestial.silly.lua.LuaGraphicsAPI;
import dev.celestial.silly.lua.BackportsAPI;
import dev.celestial.silly.lua.CollectionAPI;
import dev.celestial.silly.lua.SillyAPI;
import dev.celestial.silly.lua.SillyCompatsAPI;
import dev.celestial.silly.lua.SillyProfiler;
import dev.celestial.silly.lua.compat.VivecraftCompatAPI;
import org.figuramc.figura.lua.docs.FiguraDocsManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mixin(value = FiguraDocsManager.class, remap = false)
public class FiguraDocsManagerMixin {
    @Shadow
    @Final
    private static Map<String, Collection<Class<?>>> GLOBAL_CHILDREN;

    @Shadow
    @Final
    private static Map<Class<?>, String> NAME_MAP;

    static {
        GLOBAL_CHILDREN.put("silly", List.of(SillyAPI.class, BackportsAPI.class, SillyAPI.SillyVehicleAPI.class, SillyProfiler.class, LuaGraphicsAPI.class, CollectionAPI.class, SillyCompatsAPI.class, VivecraftCompatAPI.class));

        NAME_MAP.put(SillyEnums.GUI_ELEMENT.class, "SillyGUIElement");
        NAME_MAP.put(SillyEnums.FORMAT_LEVEL.class, "SillyFormatLevel");
    }
}
