//? if fabric {
package dev.celestial.silly.loaders.fabric;

import dev.celestial.silly.SillyPlugin;
import com.mojang.logging.LogUtils;
import dev.celestial.silly.loaders.ISillyLoader;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

public class FabricEntrypoint implements ModInitializer, ISillyLoader {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        SillyPlugin.initialize();
    }

    @Override
    public boolean isModLoaded(String mod_id) {
        return FabricLoader.getInstance().isModLoaded(mod_id);
    }
}
//?}
