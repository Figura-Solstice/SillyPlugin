package dev.celestial.silly.mixin.luaj_memory;

import dev.celestial.silly.SillyUtil;
import dev.celestial.silly.luaj.LuaOOM;
import dev.celestial.silly.luaj.memory.AllocationTracker;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.figuramc.figura.math.vector.FiguraVector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FiguraVec2.class, remap = false)
public class FiguraVec2Mixin {
    @Inject(method = "of()Lorg/figuramc/figura/math/vector/FiguraVec2;", at = @At("TAIL"))
    private static void silly$track(CallbackInfoReturnable<FiguraVector<?, ?>> cir) throws LuaOOM {
        var alloc = SillyUtil.getExecutingAllocationTracker();
        if (alloc.isPresent())
            alloc.get().track(cir.getReturnValue(), AllocationTracker.VEC2_SIZE);
    }
}
