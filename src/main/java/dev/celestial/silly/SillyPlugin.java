package dev.celestial.silly;

import dev.celestial.silly.loaders.ISillyLoader;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class SillyPlugin {
    public static final String MOD_ID = "sillyplugin";
    public static Logger LOGGER = LoggerFactory.getLogger("SillyPlugin");
    public static ISillyLoader Loader;
    @Nullable
    public static SillyAPI hostInstance;
    public static Permissions BUMPSCOCITY = new Permissions("BUMPSCOCITY", 0, 1000, 0, 0, 0, 0, 0);
    public static Permissions FAKE_BLOCKS = new Permissions("FAKE_BLOCKS", 0, 0, 0, 0, 0);
    public static Permissions SCRIPT_EXEC = new Permissions("SCRIPT_EXEC", 0, 2, 2, 2, 2, 2, 2);
    public static Permissions EXEC_TIME = new Permissions("EXEC_TIME", 0, 1000 * 60, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    public static ConcurrentHashMap<UUID, ConcurrentHashMap<BlockPos, BlockState>> FakeBlocks = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<BlockPos, Pair<BlockState, BlockEntity>> RealBlocks = new ConcurrentHashMap<>();

    public static boolean shouldHide(SillyEnums.GUI_ELEMENT el) {
        if (hostInstance == null) return false;
        if (AvatarManager.panic) return false;
        return hostInstance.disabledElements.contains(el);
    }

    public static boolean fakeExistsAt(BlockPos pos, boolean rebuild) {
        return flattenedFakes(rebuild).containsKey(pos);
    }

    public static boolean fakeExistsAt(BlockPos pos) {
        return fakeExistsAt(pos, true);
    }

    public static ConcurrentHashMap<BlockPos, BlockState> _cachedFlattened = new ConcurrentHashMap<>();
    public static boolean _cacheDirty = true;
    public static ConcurrentHashMap<BlockPos, BlockState> flattenedFakes() {
        return flattenedFakes(true);
    }
    public static ConcurrentHashMap<BlockPos, BlockState> flattenedFakes(boolean rebuild) {
        if (_cacheDirty && rebuild) {
            ConcurrentHashMap<BlockPos, BlockState> ret = new ConcurrentHashMap<>();
            for (ConcurrentHashMap<BlockPos, BlockState> value : FakeBlocks.values()) {
                ret.putAll(value);
            }
            _cachedFlattened = ret;
            _cacheDirty = false;
            return ret;
        }
        return _cachedFlattened;
    }

    public static void markFakesDirty() {
        _cacheDirty = true;
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

    public static void initialize(ISillyLoader loader) {
        Loader = loader;
        PermissionManager.CUSTOM_PERMISSIONS.put("sillyplugin", List.of(BUMPSCOCITY, FAKE_BLOCKS, SCRIPT_EXEC, EXEC_TIME));
    }
}
