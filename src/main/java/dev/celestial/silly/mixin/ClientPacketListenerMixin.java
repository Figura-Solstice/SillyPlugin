package dev.celestial.silly.mixin;

import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import dev.celestial.silly.SillyPlugin;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.figuramc.figura.avatar.AvatarManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Dictionary;
import java.util.Iterator;
import java.util.Map;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Shadow
    private ClientLevel level;

    @Inject(method = "handleBlockUpdate", at = @At("HEAD"), cancellable = true)
    public void handleBlockUpdateMixin(ClientboundBlockUpdatePacket clientboundBlockUpdatePacket, CallbackInfo ci) {
        if (level == null || !level.isClientSide) return;
        BlockPos updated = clientboundBlockUpdatePacket.getPos();
        var fakes = SillyPlugin.flattenedFakes();
        BlockState fake = fakes.get(updated);
        if (fake != null) {
            BlockState state = clientboundBlockUpdatePacket.getBlockState();
            BlockEntity entity = null;
            if (state.hasBlockEntity()) {
                entity = level.getBlockEntity(updated);
            }
            Pair<BlockState, BlockEntity> realData = new ImmutablePair<>(state, entity);
            Minecraft.getInstance().execute(() -> SillyPlugin.RealBlocks.put(updated, realData));
            if (!AvatarManager.panic && !(SillyPlugin.hostInstance != null && SillyPlugin.hostInstance.fakeBlocksDisabled)) {
                ci.cancel();
                return;
            }
        }
    }
}
