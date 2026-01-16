package dev.celestial.silly.mixin;

import dev.celestial.silly.SillyPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Dictionary;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "handleBlockUpdate", at = @At("TAIL"))
    public void handleBlockUpdateMixin(ClientboundBlockUpdatePacket clientboundBlockUpdatePacket, CallbackInfo ci) {
        SillyPlugin.FakeBlocks.elements().asIterator().forEachRemaining(data -> {
            data.keys().asIterator().forEachRemaining(pos -> {
                BlockState state = data.get(pos);
                if (Minecraft.getInstance().level != null)
                    Minecraft.getInstance().level.setBlock(pos, state, 2);
            });
        });
    }
}
