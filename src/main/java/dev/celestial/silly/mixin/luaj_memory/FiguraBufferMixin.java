package dev.celestial.silly.mixin.luaj_memory;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.celestial.silly.SillyUtil;
import dev.celestial.silly.luaj.LuaOOM;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.api.data.FiguraBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FiguraBuffer.class)
public class FiguraBufferMixin {
    @Shadow
    private byte[] buf;

    @Inject(method = "<init>(Lorg/figuramc/figura/avatar/Avatar;)V", at = @At("TAIL"))
    public void silly$trackBuffer(Avatar parent, CallbackInfo ci) throws LuaOOM {
        var alloc = SillyUtil.getExecutingAllocationTracker();
        if (alloc.isPresent())
            alloc.get().track(buf);
    }

    @Inject(method = "<init>(Lorg/figuramc/figura/avatar/Avatar;I)V", at = @At("TAIL"))
    public void silly$trackBuffer(Avatar parent, int cap, CallbackInfo ci) throws LuaOOM {
        var alloc = SillyUtil.getExecutingAllocationTracker();
        if (alloc.isPresent())
            alloc.get().track(buf);
    }

    @WrapOperation(method = "ensureBufCapacity", at = @At(value = "INVOKE", target = "Ljava/util/Arrays;copyOf([BI)[B"))
    private byte[] waa(byte[] original, int newLength, Operation<byte[]> original_) throws LuaOOM {
        var ret = original_.call(original, newLength);
        var alloc = SillyUtil.getExecutingAllocationTracker();
        if (alloc.isPresent())
            alloc.get().track(ret);
        return ret;
    }
}
