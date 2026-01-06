package dev.celestial.silly;

import com.mojang.logging.LogUtils;
import dev.celestial.silly.lua.SillyAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.permissions.PermissionManager;
import org.figuramc.figura.permissions.Permissions;
import org.slf4j.Logger;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class SillyPlugin {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static SillyAPI hostInstance;
    public static Permissions BUMPSCOCITY = new Permissions("BUMPSCOCITY", 0, 1000, 0, 0, 0, 0, 0);
    public static Permissions FAKE_BLOCKS = new Permissions("FAKE_BLOCKS", 0, 0, 0, 0, 1);
    public static Permissions COLLIDERS = new Permissions("COLLIDERS", 0, 0, 0, 0, 1);
    public static Dictionary<String, Dictionary<BlockPos, BlockState>> FakeBlocks = new Hashtable<>();

    public static boolean shouldNoclip() {
        if (hostInstance == null) return false;
        if (AvatarManager.panic) return false;
        if (!hostInstance.cheatsEnabled()) return false;
        return hostInstance.noclip;
    }

    public static void initialize() {
        PermissionManager.CUSTOM_PERMISSIONS.put("sillyplugin", List.of(BUMPSCOCITY, FAKE_BLOCKS, COLLIDERS));
    }
}
