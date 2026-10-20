package dev.celestial.silly.mixin.docs;

import dev.celestial.silly.SillyEnums;
import dev.celestial.silly.lua.*;
import dev.celestial.silly.lua.data.SillySoundEventData;
import org.figuramc.figura.lua.docs.FiguraDocsManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
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
        // SillyCompatsAPI is deliberately not included here.
        // the reason for this is that having documentation on it
        // is practically useless. not only can we not properly
        // type the fields on it, it also practically acts as
        // a simple table.
        //
        // however, of course the loaded compats will be included.
        var sillies = new ArrayList<>(List.of(
                SillyAPI.class,
                BackportsAPI.class,
                SillyAPI.SillyVehicleAPI.class,
                SillyProfiler.class,
                LuaGraphicsAPI.class,
                CollectionAPI.class,
                SillySoundEventData.class
        ));

        sillies.addAll(SillyCompatsAPI.getLoaded());

        GLOBAL_CHILDREN.put("silly", sillies);

        NAME_MAP.put(SillyEnums.GUI_ELEMENT.class, "SillyGUIElement");
        NAME_MAP.put(SillyEnums.FORMAT_LEVEL.class, "SillyFormatLevel");
        NAME_MAP.put(SillyEnums.VR_BODY_PART.class, "SillyVRBodyPart");
    }
}
