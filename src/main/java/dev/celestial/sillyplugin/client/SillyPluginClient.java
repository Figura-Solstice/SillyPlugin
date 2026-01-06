package dev.celestial.sillyplugin.client;

import dev.celestial.sillyplugin.SillyPlugin;
import dev.celestial.sillyplugin.lua.SillyAPI;
import net.fabricmc.api.ClientModInitializer;
import org.figuramc.figura.permissions.PermissionManager;
import org.figuramc.figura.permissions.Permissions;

import java.util.ArrayList;
import java.util.List;

public class SillyPluginClient implements ClientModInitializer {
    public static SillyAPI hostInstance;

    @Override
    public void onInitializeClient() {
        PermissionManager.CUSTOM_PERMISSIONS.put("sillyplugin", List.of(SillyPlugin.BUMPSCOCITY));
    }
}
