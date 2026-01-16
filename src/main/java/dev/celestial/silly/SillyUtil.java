package dev.celestial.silly;

import com.mojang.authlib.GameProfile;
import dev.celestial.silly.mixin.MinecraftAccessor;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SillyUtil {
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

    // Code derived from
    public static void antiGhost() {
        Minecraft mc = Minecraft.getInstance();
        ClientPacketListener conn = mc.getConnection();
        if (conn == null)
            return;
        BlockPos pos = mc.player.blockPosition();
        for (int dx = -4; dx <= 4; dx++)
            for (int dy = -4; dy <= 4; dy++)
                for (int dz = -4; dz <= 4; dz++) {
                    ServerboundPlayerActionPacket packet = new ServerboundPlayerActionPacket(
                            ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK,
                            new BlockPos(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz),
                            Direction.UP // with ABORT_DESTROY_BLOCK, this value is unused
                    );
                    conn.send(packet);
                }
    }
}
