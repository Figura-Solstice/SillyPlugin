package dev.celestial.sillyplugin;

import com.mojang.authlib.GameProfile;
import dev.celestial.sillyplugin.mixin.MinecraftAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
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
}
