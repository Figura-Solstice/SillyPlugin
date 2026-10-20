package dev.celestial.silly.mixin.luaj_memory;

import dev.celestial.silly.SillyUtil;
import dev.celestial.silly.luaj.LuaOOM;
import dev.celestial.silly.luaj.memory.AllocationTracker;
import org.figuramc.figura.math.matrix.FiguraMat2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = org.figuramc.figura.math.matrix.FiguraMat2.class, remap = false)
public class FiguraMat2Mixin {
    @Inject(method = "of()Lorg/figuramc/figura/math/matrix/FiguraMat2;", at = @At("RETURN"))
    private static void silly$track(CallbackInfoReturnable<FiguraMat2> cir) throws LuaOOM {
        var alloc = SillyUtil.getExecutingAllocationTracker();
        if (alloc.isPresent())
            alloc.get().track(cir.getReturnValue(), AllocationTracker.MAT2_SIZE);
    }
}
