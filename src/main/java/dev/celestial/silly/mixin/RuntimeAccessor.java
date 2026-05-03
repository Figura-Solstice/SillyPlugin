package dev.celestial.silly.mixin;

import org.figuramc.figura.lua.FiguraLuaRuntime;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.Varargs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = FiguraLuaRuntime.class, remap = false)
public interface RuntimeAccessor {
    @Accessor("scripts")
    public Map<String, String> getScripts();
    @Accessor("getInfoFunction")
    public LuaFunction getGetInfoFunction();
    @Accessor("setHookFunction")
    public LuaFunction getSetHookFunction();
    @Accessor("loadedScripts")
    public Map<String, Varargs> getLoadedScripts();
    @Accessor("userGlobals")
    public Globals getGlobals();
}
