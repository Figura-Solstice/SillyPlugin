package dev.celestial.silly.mixin.events;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.celestial.silly.SillyUtil;
import org.figuramc.figura.backend2.websocket.S2CMessageHandler;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.gui.FiguraToast;
import org.figuramc.figura.utils.FiguraText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

@Mixin(S2CMessageHandler.class)
public class S2CMessageHandlerMixin {
    @WrapMethod(method = "notice")
    private static void silly$pingFailEvent(ByteBuffer bytes, Operation<Void> original) {
        if (Configs.CONNECTION_TOASTS.value) original.call(bytes);
        else {
            SillyUtil.getHostAvatar().ifPresent(host -> {
                byte type = bytes.get();
                switch (type) {
                    case 0 -> host.run("PING_FAILED", host.tick, "PING_SIZE");
                    case 1 -> host.run("PING_FAILED", host.tick, "PING_RATE");
                }
            });
        }
    }

    @Inject(method = "notice", at = @At("TAIL"))
    private static void silly$pingFailEvent(ByteBuffer bytes, CallbackInfo ci, @Local(name = "type") byte type) {
        SillyUtil.getHostAvatar().ifPresent(host -> {
            switch (type) {
                case 0 -> host.run("PING_FAILED", host.tick, "PING_SIZE");
                case 1 -> host.run("PING_FAILED", host.tick, "PING_RATE");
            }
        });
    }
}
