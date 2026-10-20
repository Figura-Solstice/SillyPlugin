package dev.celestial.silly.mixin;

import dev.celestial.silly.not_a_mixin.ReloadableTextureExtensions;
//? if 1.21.4 {
import net.minecraft.client.renderer.texture.ReloadableTexture;
//?}



import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import org.figuramc.figura.avatar.Avatar;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

//? if 1.21.4 {
@Mixin(ReloadableTexture.class)
//?} else {
/*@Mixin(SimpleTexture.class)
*///?}
public class ReloadableTextureMixin implements ReloadableTextureExtensions {
    @Final
    @Shadow
    @Mutable
    //? if 1.21.4 {
    private ResourceLocation resourceId;
    //?} else {
    /*protected ResourceLocation location;
    *///?}

    @Override
    public void sillyPlugin$setIdentifier(ResourceLocation loc) {
        //? if 1.21.4 {
        this.resourceId = loc;
         //?} else {
        /*this.location = loc;
        *///?}
    }
}
