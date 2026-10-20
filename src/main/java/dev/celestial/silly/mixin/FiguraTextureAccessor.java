package dev.celestial.silly.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.model.rendering.texture.FiguraTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FiguraTexture.class)
public interface FiguraTextureAccessor {
    @Accessor("texture")
    NativeImage silly$getTexture();

    @Accessor("owner")
    Avatar silly$getOwner();

    @Accessor("isClosed")
    boolean silly$getClosed();

    @Invoker("backupImage")
    void silly$invokeBackupImage();

    @Invoker("parseColor")
    FiguraVec4 silly$invokeParseColor(String method, Object r, Double g, Double b, Double a);
}
