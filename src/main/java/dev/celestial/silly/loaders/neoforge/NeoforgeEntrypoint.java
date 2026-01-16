//? if neoforge {
/*package dev.celestial.silly.loaders.neoforge;

import dev.celestial.silly.SillyPlugin;
import com.mojang.logging.LogUtils;
import dev.celestial.silly.loaders.ISillyLoader;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod("sillyplugin")
public class NeoforgeEntrypoint implements ISillyLoader {
    private static final Logger LOGGER = LogUtils.getLogger();

    public NeoforgeEntrypoint() {
        SillyPlugin.initialize();
    }

    @Override
    public boolean isModLoaded(String mod_id) {
        return ModList.get().isLoaded(mod_id);
    }
}
*///?}
