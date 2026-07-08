package dev.celestial.silly.lua;

import dev.celestial.silly.SillyPlugin;
import dev.celestial.silly.lua.compat.BaseCompatAPI;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.FiguraLuaRuntime;
import org.figuramc.figura.lua.LuaWhitelist;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@LuaWhitelist
public class SillyCompatsAPI {
    private static final Map<String, SillyCompatDefinition> compats = new HashMap<>() {{
       put("voicechat", new SillyCompatDefinition("voicechat", "svc", "dev.celestial.silly.lua.compat.VoicechatCompatAPI"));
       put("vivecraft", new SillyCompatDefinition("vivecraft", "vivecraft", "dev.celestial.silly.lua.compat.VivecraftCompatAPI"));
    }};
    private final Avatar avatar;
    private final FiguraLuaRuntime runtime;
    private final Map<String, BaseCompatAPI> instances = new HashMap<>();
    @LuaWhitelist
    public BaseCompatAPI svc; // for print()
    @LuaWhitelist
    public BaseCompatAPI vivecraft; // for print()

    public static List<Class<?>> getLoaded() {
        var list = new ArrayList<Class<?>>();
        list.add(BaseCompatAPI.class);
        for (var entry : compats.entrySet()) {
            if (SillyPlugin.Loader.isModLoaded(entry.getKey())) {
                try {
                    list.add(Class.forName(entry.getValue().classPath));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return list;
    }

    public SillyCompatsAPI(FiguraLuaRuntime runtime) {
        this.avatar = runtime.owner;
        this.runtime = runtime;
        for (var entry : compats.entrySet()) {
            if (SillyPlugin.Loader.isModLoaded(entry.getKey())) {
                try {
                    Class<? extends BaseCompatAPI> clazz = (Class<? extends BaseCompatAPI>) Class.forName(entry.getValue().classPath);
                    instances.put(entry.getValue().name, clazz.getConstructor(FiguraLuaRuntime.class).newInstance(runtime));
                } catch (ClassNotFoundException | ClassCastException | InvocationTargetException |
                         InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @LuaWhitelist
    public Object __index(String key) {
        if (instances.containsKey(key)) {
            return instances.get(key);
        }
        return null;
    }

    public record SillyCompatDefinition(String modId, String name, String classPath) {
    }
}
