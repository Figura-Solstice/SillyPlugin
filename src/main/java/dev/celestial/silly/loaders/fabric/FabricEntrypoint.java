//? if fabric {
/*package dev.celestial.silly.loaders.fabric;

import dev.celestial.silly.SillyPlugin;
import com.mojang.logging.LogUtils;
import dev.celestial.silly.loaders.ISillyLoader;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import org.slf4j.Logger;

import java.util.Optional;

public class FabricEntrypoint implements ModInitializer, ISillyLoader {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        SillyPlugin.initialize(this);
    }

    @Override
    public boolean isModLoaded(String mod_id) {
        return FabricLoader.getInstance().isModLoaded(mod_id);
    }

    @Override
    public String getModVersion(String mod_id) {
        Optional<ModContainer> container = FabricLoader.getInstance().getModContainer(mod_id);
        return container.map(modContainer -> modContainer.getMetadata().getVersion().getFriendlyString()).orElse(null);
    }
}
*///?}
