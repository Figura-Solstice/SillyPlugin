package dev.celestial.silly.mixin;

import dev.celestial.silly.SillyPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.world.level.Level;
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
            Minecraft mc = Minecraft.getInstance();
            Level lvl = mc.level;
            if (lvl != null && lvl.isClientSide)
                data.keys().asIterator().forEachRemaining(pos -> {
                    BlockState state = data.get(pos);
                    mc.level.setBlock(pos, state, 2);
                });
        });
    }
}
