package dev.celestial.silly;

import com.mojang.authlib.GameProfile;
import dev.celestial.silly.lua.SillyAPI;
import dev.celestial.silly.mixin.MinecraftAccessor;
import dev.celestial.silly.not_a_mixin.AvatarAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.Services;
import net.minecraft.world.entity.player.Player;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SillyUtil {
    public static final boolean DEV_MODE = false;
    private static Services services;
    public static Avatar getAvatar(String username) {
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

        if (uuid == null) {
            return null;
        }

        return AvatarManager.getAvatarForPlayer(uuid);
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
        SillyAPI api = ((AvatarAccessor)avatar).silly$getSilly();
        return canCheat(api);
    }

    public static boolean canCheat() {
        return canCheat(SillyPlugin.hostInstance);
    }
}
