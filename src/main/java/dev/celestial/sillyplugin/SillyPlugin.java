package dev.celestial.sillyplugin;

import net.fabricmc.api.ModInitializer;
import org.figuramc.figura.permissions.Permissions;

public class SillyPlugin implements ModInitializer {
    public static Permissions BUMPSCOCITY = new Permissions("BUMPSCOCITY", 0, 1000, 0, 0, 0, 0, 0);

    @Override
    public void onInitialize() {
    }
}
