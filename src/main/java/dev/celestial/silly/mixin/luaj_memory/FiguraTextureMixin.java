package dev.celestial.silly.mixin.luaj_memory;

import com.mojang.blaze3d.platform.NativeImage;
import dev.celestial.silly.SillyUtil;
import dev.celestial.silly.luaj.LuaOOM;
import dev.celestial.silly.luaj.memory.AllocationTracker;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.model.rendering.texture.FiguraTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FiguraTexture.class)
public class FiguraTextureMixin {
    @Shadow
    @Final
    private NativeImage texture;

    @Inject(method = "<init>(Lorg/figuramc/figura/avatar/Avatar;Ljava/lang/String;[B)V", at = @At("TAIL"))
    public void silly$track(Avatar owner, String name, byte[] data, CallbackInfo ci) throws LuaOOM {
        var alloc = SillyUtil.getExecutingAllocationTracker();
        if (alloc.isPresent())
            alloc.get().track(this.texture, AllocationTracker.OBJECT_SIZE + AllocationTracker.BYTE_SIZE * 4 * this.texture.getWidth() * this.texture.getHeight());
    }

    @Inject(method = "<init>(Lorg/figuramc/figura/avatar/Avatar;Ljava/lang/String;Lcom/mojang/blaze3d/platform/NativeImage;)V", at = @At("TAIL"))
    public void silly$track(Avatar owner, String name, NativeImage image, CallbackInfo ci) throws LuaOOM {
        var alloc = SillyUtil.getExecutingAllocationTracker();
        if (alloc.isPresent())
            alloc.get().track(image, AllocationTracker.OBJECT_SIZE + AllocationTracker.BYTE_SIZE * 4 * image.getWidth() * image.getHeight());
    }

    @Inject(method = "<init>(Lorg/figuramc/figura/avatar/Avatar;Ljava/lang/String;II)V", at = @At("TAIL"))
    public void silly$track(Avatar owner, String name, int width, int height, CallbackInfo ci) throws LuaOOM {
        var alloc = SillyUtil.getExecutingAllocationTracker();
        if (alloc.isPresent())
            alloc.get().track(this.texture, AllocationTracker.OBJECT_SIZE + AllocationTracker.BYTE_SIZE * 4 * width * height);
    }
}
