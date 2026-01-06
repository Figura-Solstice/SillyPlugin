package dev.celestial.sillyplugin.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.celestial.sillyplugin.SillyPlugin;
import dev.celestial.sillyplugin.client.SillyPluginClient;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public class PlayerMixin {
    @WrapOperation(method="tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isSpectator()Z"))
    public boolean tickMixin(Player instance, Operation<Boolean> original) {
        return original.call(instance) || SillyPluginClient.shouldNoclip();
    }
}
