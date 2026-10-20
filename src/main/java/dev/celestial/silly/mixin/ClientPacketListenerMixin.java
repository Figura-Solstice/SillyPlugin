package dev.celestial.silly.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.celestial.silly.SillySettings;
import dev.celestial.silly.helper.SillyBlockContainer;
import dev.celestial.silly.helper.SillyBlockHandler;
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

    //? if >=1.21.4 {
    @WrapOperation(method = "handleSetTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;setTimeFromServer(JJZ)V"))
    public void silly$daytimeOverride(ClientLevel instance, long l, long m, boolean bl, Operation<Void> original) {
        var hostSilly = SillyPlugin.hostInstance;
        if (hostSilly == null || !(hostSilly.cheatsEnabled() && hostSilly.dayTime.isOverridden())) {
            original.call(instance, l, m, bl);
            return;
        }
        original.call(instance, l, hostSilly.dayTime.getValue(), false);
    }
    //?} else {
    /*@WrapOperation(method = "handleSetTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;setDayTime(J)V"))
    public void silly$daytimeOverride(ClientLevel instance, long l, Operation<Void> original) {
        var hostSilly = SillyPlugin.hostInstance;
        if (hostSilly == null || !(hostSilly.cheatsEnabled() && hostSilly.dayTime.isOverridden())) {
            original.call(instance, l);
            return;
        }
        original.call(instance, hostSilly.dayTime.getValue());
    }*///?}

    @Inject(method = "handleBlockUpdate", at = @At("HEAD"), cancellable = true)
    public void handleBlockUpdateMixin(ClientboundBlockUpdatePacket clientboundBlockUpdatePacket, CallbackInfo ci) {
        if (level == null || !level.isClientSide || !SillySettings.CHEATS.getBool()) return;
        BlockPos updated = clientboundBlockUpdatePacket.getPos();
        if (SillyPlugin.fakeExistsAt(updated)) {
            BlockState state = clientboundBlockUpdatePacket.getBlockState();
            BlockEntity entity;
            if (state.hasBlockEntity()) {
                entity = level.getBlockEntity(updated);
            } else {
                entity = null;
            }
            Minecraft.getInstance().execute(() -> {
                SillyBlockHandler.REAL_BLOCKS.put(updated, new SillyBlockContainer(updated, state, entity));
                if (!AvatarManager.panic && !(SillyPlugin.hostInstance != null && SillyPlugin.hostInstance.fakeBlocksDisabled)) {
                    SillyBlockHandler.setBlock(SillyBlockHandler.BLOCKS.get(updated), level);
                }
            });
            if (!AvatarManager.panic && !(SillyPlugin.hostInstance != null && SillyPlugin.hostInstance.fakeBlocksDisabled)) {
                ci.cancel();
            }
        }
    }
}
