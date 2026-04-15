package dev.celestial.silly.mixin;

import dev.celestial.silly.SillyUtil;
import dev.celestial.silly.helper.LuaGraphics;
import dev.celestial.silly.lua.BackportsAPI;
import dev.celestial.silly.lua.DevAPI;
import dev.celestial.silly.lua.SillyAPI;
import dev.celestial.silly.lua.SillyProfiler;
import dev.celestial.silly.not_a_mixin.AvatarAccessor;
import org.figuramc.figura.lua.FiguraAPIManager;
import org.figuramc.figura.lua.FiguraLuaRuntime;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Mixin(value = FiguraAPIManager.class, remap = false)
public class FiguraAPIManagerMixin {


    @Shadow
    @Final
    public static Set<Class<?>> WHITELISTED_CLASSES;

    @Shadow
    @Final
    public static Map<String, Function<FiguraLuaRuntime, Object>> API_GETTERS;

    static {
        WHITELISTED_CLASSES.add(SillyAPI.class);
        WHITELISTED_CLASSES.add(BackportsAPI.class);
        WHITELISTED_CLASSES.add(LuaGraphics.class);
        WHITELISTED_CLASSES.add(SillyProfiler.class);
        if (SillyUtil.DEV_MODE) {
            WHITELISTED_CLASSES.add(DevAPI.class);
            API_GETTERS.put("silly_dev", r -> r.owner.isHost ? new DevAPI(r) : null);
        }

        API_GETTERS.put("silly", r -> ((AvatarAccessor)r.owner).silly$setSilly(new SillyAPI(r.owner)));
        API_GETTERS.put("silly_backports", BackportsAPI::new);
    }
}
