package dev.celestial.silly;

import dev.celestial.silly.lua.SillyAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.permissions.PermissionManager;
import org.figuramc.figura.permissions.Permissions;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SillyPlugin {
    public static Logger LOGGER = LoggerFactory.getLogger("SillyPlugin");
    @Nullable
    public static SillyAPI hostInstance;
    public static Permissions BUMPSCOCITY = new Permissions("BUMPSCOCITY", 0, 1000, 0, 0, 0, 0, 0);
    public static Permissions FAKE_BLOCKS = new Permissions("FAKE_BLOCKS", 0, 0, 0, 0, 1);
//    public static Permissions COLLIDERS = new Permissions("COLLIDERS", 0, 0, 0, 0, 1);
    public static Map<UUID, Map<BlockPos, BlockState>> FakeBlocks = new HashMap<>();
    public static Map<BlockPos, Pair<BlockState, BlockEntity>> RealBlocks = new HashMap<>();

    public static boolean shouldHide(SillyEnums.GUI_ELEMENT el) {
        if (hostInstance == null) return false;
        if (AvatarManager.panic) return false;
        return hostInstance.disabledElements.contains(el);
    }

    public static boolean fakeExistsAt(BlockPos pos) {
        return flattenedFakes().containsKey(pos);
    }

    public static Map<BlockPos, BlockState> flattenedFakes() {
        HashMap<BlockPos, BlockState> ret = new HashMap<>();
        for (Map<BlockPos, BlockState> value : FakeBlocks.values()) {
            ret.putAll(value);
        }
        return ret;
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
