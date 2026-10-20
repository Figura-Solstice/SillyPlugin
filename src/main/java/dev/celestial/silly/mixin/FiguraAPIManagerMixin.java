package dev.celestial.silly.mixin;

import dev.celestial.silly.SillySettings;
import dev.celestial.silly.SillyUtil;
import dev.celestial.silly.lua.data.SillySoundEventData;
import dev.celestial.silly.lua.BackportWrapper.BackportedTexture;
import dev.celestial.silly.lua.LuaGraphicsAPI;
import dev.celestial.silly.lua.*;
import dev.celestial.silly.not_a_mixin.AvatarExtensions;
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
        WHITELISTED_CLASSES.add(LuaGraphicsAPI.class);
        WHITELISTED_CLASSES.add(SillyProfiler.class);
        WHITELISTED_CLASSES.add(SillyAPI.SillyVehicleAPI.class);
        WHITELISTED_CLASSES.add(CollectionAPI.class);
        WHITELISTED_CLASSES.add(SillyCompatsAPI.class);
        WHITELISTED_CLASSES.add(SillySoundEventData.class);
        WHITELISTED_CLASSES.add(SillyAudioEncoding.class);
        WHITELISTED_CLASSES.add(SillyAudioEncoding.SillySoundDataContainer.class);
        WHITELISTED_CLASSES.add(BackportedTexture.class);
        WHITELISTED_CLASSES.addAll(SillyCompatsAPI.getLoaded());

        if (SillyUtil.DEV_MODE) {
            WHITELISTED_CLASSES.add(DevAPI.class);
            API_GETTERS.put("silly_dev", r -> r.owner.isHost ? new DevAPI(r) : null);
        }

        API_GETTERS.put("silly", r -> {
            var should_add = r.owner.isHost ? SillySettings.SILLY_ON_HOST.getBool() : SillySettings.SILLY_ON_NONHOST.getBool();

            // has to be initialized regardless, for stuff like the profiler.
            var silly = ((AvatarExtensions)r.owner).silly$setSilly(new SillyAPI(r));

            return should_add ? silly : null;
        });

        // backward compat w/ 1.1.1
        // technically deprecated
        API_GETTERS.put("silly_backports", r -> {
            var should_add = r.owner.isHost ? SillySettings.SILLY_ON_HOST.getBool() : SillySettings.SILLY_ON_NONHOST.getBool();
            return should_add ? ((AvatarExtensions)r.owner).silly$getSilly().backports : null;
        });
    }
}
