package dev.celestial.silly.mixin.backports;

import dev.celestial.silly.lua.BackportWrapper.BackportedTexture;
import dev.celestial.silly.not_a_mixin.FiguraTextureExtensions;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.model.rendering.texture.FiguraTexture;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FiguraTexture.class, remap = false)
public class FiguraTextureMixin implements FiguraTextureExtensions {
    @Unique public BackportedTexture silly$backport;

    @Override
    public BackportedTexture silly$getBackport() {
        return silly$backport;
    }

    @Override
    public void silly$setBackport(BackportedTexture tex) {
        silly$backport = tex;
    }
}
