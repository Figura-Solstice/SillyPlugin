package dev.celestial.sillyplugin.mixin;

import dev.celestial.sillyplugin.lua.SillyAPI;
import org.figuramc.figura.lua.docs.FiguraGlobalsDocs;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(value = FiguraGlobalsDocs.class, remap = false)
public class FiguraGlobalsDocsMixin {
    @Unique
    @LuaFieldDoc("globals.silly")
    public SillyAPI silly;
}
