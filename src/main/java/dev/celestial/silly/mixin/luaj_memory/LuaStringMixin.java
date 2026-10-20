package dev.celestial.silly.mixin.luaj_memory;

import dev.celestial.silly.SillyUtil;
import dev.celestial.silly.luaj.LuaOOM;
import org.luaj.vm2.LuaString;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LuaString.class, remap = false)
public class LuaStringMixin {

    // TODO: is this really the best place to inject tracking?
    //       this won't account for offset and length. but when
    //       are those ever anything that doesnt cover the full
    //       byte array? <string>:sub()?
    @Inject(method = "<init>", at = @At("RETURN"))
    public void silly$track(byte[] bytes, int offset, int length, CallbackInfo ci) throws LuaOOM {
        var alloc = SillyUtil.getExecutingAllocationTracker();
        if (alloc.isPresent())
            alloc.get().track(bytes);
    }
}
