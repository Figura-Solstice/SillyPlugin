//? if neoforge {
/*package dev.celestial.silly.loaders.neoforge;

import dev.celestial.silly.SillyPlugin;
import com.mojang.logging.LogUtils;
import dev.celestial.silly.loaders.ISillyLoader;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(SillyPlugin.MOD_ID)
public class NeoforgeEntrypoint implements ISillyLoader {
    private static final Logger LOGGER = LogUtils.getLogger();

    public NeoforgeEntrypoint() {
        SillyPlugin.initialize(this);
    }

    @Override
    public boolean isModLoaded(String mod_id) {
        return ModList.get().isLoaded(mod_id);
    }

    @Override
    public String getModVersion(String mod_id) {
        return ModList.get().getModContainerById(SillyPlugin.MOD_ID).map(cont -> cont.getModInfo().getVersion().getQualifier()).orElse(null);
    }
}
*///?}
