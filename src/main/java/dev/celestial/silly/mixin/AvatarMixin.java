package dev.celestial.silly.mixin;

import dev.celestial.silly.lua.SillyAPI;
import dev.celestial.silly.not_a_mixin.AvatarAccessor;
import org.figuramc.figura.avatar.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Avatar.class, remap = false)
public class AvatarMixin implements AvatarAccessor {
    @Unique
    public SillyAPI silly;

    @Override
    public SillyAPI silly$getSilly() {
        return silly;
    }

    @Override
    public SillyAPI silly$setSilly(SillyAPI instance) {
        silly = instance;
        return silly;
    }

    @Inject(method = "clean", at = @At("TAIL"))
    public void cleanMixin(CallbackInfo ci) {
        SillyAPI silly = silly$getSilly();
        if (silly != null) silly.cleanup();
    }
}
