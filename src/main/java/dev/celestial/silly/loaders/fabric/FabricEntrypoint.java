//? if fabric {
package dev.celestial.silly.loaders.fabric;

import dev.celestial.silly.SillyPlugin;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;

public class FabricEntrypoint implements ModInitializer {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        SillyPlugin.initialize();
    }
}
//?}
