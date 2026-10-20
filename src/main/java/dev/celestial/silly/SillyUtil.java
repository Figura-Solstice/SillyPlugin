package dev.celestial.silly;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.NativeImage;
import dev.celestial.silly.helper.CallerContext;
import dev.celestial.silly.lua.BackportsAPI;
import dev.celestial.silly.lua.SillyAPI;
import dev.celestial.silly.luaj.LuaOOM;
import dev.celestial.silly.luaj.LuaUncatchableError;
import dev.celestial.silly.luaj.memory.AllocationTracker;
import dev.celestial.silly.luaj.memory.AllocationTrackerImpl;
import dev.celestial.silly.luaj.memory.StubAllocationTracker;
import dev.celestial.silly.luaj.memory.UnrestrictedAllocationTracker;
import dev.celestial.silly.mixin.MinecraftAccessor;
import dev.celestial.silly.not_a_mixin.AvatarExtensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Services;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.player.Player;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.mixin.render.TextureAtlasAccessor;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.VarArgFunction;
import org.spongepowered.asm.mixin.Unique;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class SillyUtil {
    //? if dev_mode {
    /*public static final boolean DEV_MODE = true;
    *///?} else {
    public static final boolean DEV_MODE = false;
    //?}
    public static Services services;
    public static Avatar getAvatar(String username) {
        UUID uuid = getUUID(username);

        if (uuid == null) {
            return null;
        }

        return AvatarManager.getAvatarForPlayer(uuid);
    }

    public static Optional<Avatar> getHostAvatar() {
        return Optional.ofNullable(AvatarManager.getAvatarForPlayer(clientUUID()));
    }


    public static final long GRACE_PERIOD_TIME = 3000000000L;
    public static final int GRACE_PERIOD_EXTRA = 128 * 1024;
    public static Function<Avatar, AllocationTracker<LuaOOM>> allocate = (avatar) -> {
        if (!SillySettings.MEMORY_TRACK.getBool()) return new StubAllocationTracker<>();
        var max = avatar.permissions.get(SillyPermissions.MEMORY) * 1024;
        if (max < 0) {
            return new UnrestrictedAllocationTracker<>();
        }
        return new AllocationTrackerImpl<LuaOOM>(max, GRACE_PERIOD_TIME, GRACE_PERIOD_EXTRA, (reason)
                -> new LuaOOM("Lua exceeded limit of " + max + "kb memory! " + (reason == AllocationTracker.ErrorReason.GRACE_EXPIRED
                    ? "(grace period expired)"
                    : "(overran grace period limit)")));
    };

    public static @Nullable AllocationTracker<LuaOOM> getAllocationTracker(UUID owner) {
        Avatar avatar = AvatarManager.getAvatarForPlayer(owner);
        if (avatar == null) return null;
        return ((AvatarExtensions)avatar).silly$getAllocationTracker();
    }

    public static Optional<AllocationTracker<LuaOOM>> getExecutingAllocationTracker() {
        var executing = BackportsAPI.callerStack.get().peek();
        if (executing == null) return Optional.empty();
        return Optional.ofNullable(getAllocationTracker(executing.getLeft()));
    }

    public static void nikoTricksTheJavaCompiler() throws LuaUncatchableError { }

    public static void logAsAvatar(Avatar avatar, Consumer<String> logFn, String msg) {
        logFn.accept("[" + avatar.entityName + "] " + msg);
    }

    // figura TextureAPI.fromVanilla
    public static NativeImage getImage(ResourceLocation loc) {
        try {
            TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(loc);
            atlas.bind();
            TextureAtlasAccessor atlasAccessor = (TextureAtlasAccessor) atlas;
            NativeImage nativeImage = new NativeImage(atlasAccessor.getWidth(), atlasAccessor.getHeight(), false);
            nativeImage.downloadTexture(0, false);
            return nativeImage;
        } catch (Exception ignored) {}
        try {
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(loc);
            if (resource.isPresent())
                return NativeImage.read(resource.get().open());
        } catch (Exception e) {
            throw new LuaError(e.getMessage());
        }
        try {
            AbstractTexture tx = Minecraft.getInstance().getTextureManager().getTexture(loc);

            NativeImage img;
            if (tx instanceof SimpleTexture stx) {
                //? if >1.21.1 {
                TextureContents conts = stx.loadContents(Minecraft.getInstance().getResourceManager());
                img = conts.image();
                //?} else {
                /*SimpleTexture.TextureImage txImg = stx.getTextureImage(Minecraft.getInstance().getResourceManager());
                img = txImg.getImage();
                *///?}
            } else if (tx instanceof DynamicTexture dtx) {
                img = dtx.getPixels();
            } else {
                throw new RuntimeException("No handling for " + tx.getClass().getSimpleName() + " added in SillyUtil.getImage!");
            }
            assert img != null;
            return img.mappedCopy(i -> i);
        } catch (Exception e) {
            throw new LuaError(e.getMessage());
        }
    }

    public static UUID getUUID(String username) {
        Minecraft mc = Minecraft.getInstance();
        if (SillyUtil.services == null) {
            SillyUtil.services = Services.create(((MinecraftAccessor)mc).silly$getAuthenticationService(), mc.gameDirectory);
        }
        UUID uuid = null;
        if (mc.level == null) return null;

        try {
            uuid = UUID.fromString(username);
        } catch (IllegalArgumentException ignored) {
            assert mc.level != null;
            List<AbstractClientPlayer> players = mc.level.players();
            for (Player current : players) {
                if (current.getName().getString().equals(username)) {
                    uuid = current.getUUID();
                    break;
                }
            }

            if (uuid == null) {
                Optional<GameProfile> profile = services.profileCache().get(username);
                if (profile.isPresent())
                    uuid = profile.get().getId();
            }
        }
        return uuid;
    }

    public static UUID clientUUID() {
        return FiguraMod.getLocalPlayerUUID();
    }

    public static LuaTable transformTableWithCaller(LuaTable table, UUID caller, UUID owner) {
        return transformTableWithCaller(table, caller, owner, new HashSet<LuaTable>());
    }

    public static LuaTable transformTableWithCaller(LuaTable table, UUID caller, UUID owner, Set<LuaTable> visited) {
        if (visited.contains(table))
            return table; // no
        visited.add(table);
        LuaTable ret = new LuaTable();

        for (LuaValue key : table.keys()) {
            LuaValue value = table.rawget(key);
            if (value.isfunction()) {
                ret.rawset(key, transformFunctionWithCaller(value.checkfunction(), caller, owner));
            } else if (value.istable()) {
                ret.rawset(key, transformTableWithCaller(value.checktable(), caller, owner, visited));
            } else {
                ret.rawset(key, value);
            }
        }
        return ret;
    }

    public static LuaValue transformFunctionWithCaller(LuaFunction func, UUID caller, UUID owner) {
        return new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                try(CallerContext ctx = BackportsAPI.openCallerContext(caller, owner, "TransformedFunction/" + func.hashCode())) {
                    return func.invoke(args);
                }
            }

            @Override
            public String tojstring() {
                return func.tojstring();
            }
        };
    }

    public static void Devlog(String format, Object... vars) {
        if (DEV_MODE && SillySettings.DEV_LOGS.getBool())
            SillyPlugin.LOGGER.info(format, vars);
    }

    private static Class<?> roltClass;
    public static LuaTable createReadOnlyLuaTable(LuaTable tbl) {
        if (roltClass != null) {
            try {
                return (LuaTable) roltClass.getConstructor(LuaValue.class).newInstance(tbl);
            } catch (Exception e) {
                throw new LuaError(e);
            }
        }
        try {
            roltClass = Class.forName("org.figuramc.figura.lua.ReadOnlyLuaTable");
        } catch (Exception e) {
            try {
                roltClass = Class.forName("org.figuramc.figura.lua.transfer.ReadOnlyLuaTable");
            } catch (ClassNotFoundException ex) {
                throw new LuaError("Could not create ReadOnlyLuaTable!");
            }
        }
        try {
            return (LuaTable) roltClass.getConstructor(LuaValue.class).newInstance(tbl);
        } catch (Exception e) {
            throw new LuaError(e);
        }
    }

    public static boolean canCheat(SillyAPI api) {
        if (AvatarManager.panic) return false;
        if (api == null) return false;
        return api.cheatsEnabled();
    }

    public static boolean canCheat(Avatar avatar) {
        SillyAPI api = ((AvatarExtensions)avatar).silly$getSilly();
        return canCheat(api);
    }

    public static boolean canCheat() {
        return canCheat(SillyPlugin.hostInstance);
    }

    public static ResourceLocation location(String namespace, String path) {
        //? if >=1.21 {
        ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(namespace, path);
        //?} else {
        /*ResourceLocation loc = ResourceLocation.tryBuild(namespace, path);
         *///?}
        return loc;
    }
}
