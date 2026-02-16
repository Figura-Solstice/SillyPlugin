package dev.celestial.silly;

import dev.celestial.silly.lua.SillyAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.permissions.PermissionManager;
import org.figuramc.figura.permissions.Permissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

public class SillyPlugin {
    public static Logger LOGGER = LoggerFactory.getLogger("SillyPlugin");
    public static SillyAPI hostInstance;
    public static Permissions BUMPSCOCITY = new Permissions("BUMPSCOCITY", 0, 1000, 0, 0, 0, 0, 0);
    public static Permissions FAKE_BLOCKS = new Permissions("FAKE_BLOCKS", 0, 0, 0, 0, 1);
//    public static Permissions COLLIDERS = new Permissions("COLLIDERS", 0, 0, 0, 0, 1);
    public static Dictionary<UUID, Dictionary<BlockPos, BlockState>> FakeBlocks = new Hashtable<>();

    public static boolean shouldHide(SillyEnums.GUI_ELEMENT el) {
        if (hostInstance == null) return false;
        if (AvatarManager.panic) return false;
        return hostInstance.disabledElements.contains(el);
    }

    public static boolean shouldNoclip(Entity entity) {
        if (hostInstance == null) return false;
        if (AvatarManager.panic) return false;
        if (entity instanceof Player plr) {
            if (plr.isLocalPlayer() || (plr.getServer() != null && !plr.getServer().isDedicatedServer()))
                return hostInstance.noclip && hostInstance.cheatsEnabled();
        }
        return false;
    }

    public static void initialize() {
        PermissionManager.CUSTOM_PERMISSIONS.put("sillyplugin", List.of(BUMPSCOCITY, FAKE_BLOCKS/*, COLLIDERS*/));
    }
}
