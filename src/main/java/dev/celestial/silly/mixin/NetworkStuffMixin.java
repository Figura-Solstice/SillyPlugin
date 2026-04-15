package dev.celestial.silly.mixin;

import dev.celestial.silly.SillyPlugin;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.backend2.NetworkStuff;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mixin(NetworkStuff.class)
public class NetworkStuffMixin {
    @Redirect(method = "tickSubscriptions", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;getOnlinePlayerIds()Ljava/util/Collection;"))
    private static Collection<UUID> tickSubscriptionMixin(ClientPacketListener instance) {
        Collection<UUID> orig = instance.getOnlinePlayerIds();
        Collection<UUID> ret = new HashSet<>();
        if (SillyPlugin.hostInstance == null) return orig;
        if (AvatarManager.panic) return orig;
        Set<UUID> custom = SillyPlugin.hostInstance.customSubscriptions;
        ret.addAll(orig);
        ret.addAll(custom);
        return ret;
    }
}
