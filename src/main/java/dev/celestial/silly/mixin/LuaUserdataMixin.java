package dev.celestial.silly.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.celestial.silly.lua.BackportWrapper.BackportedTexture;
import org.figuramc.figura.model.rendering.texture.FiguraTexture;
import org.luaj.vm2.LuaUserdata;
import org.luaj.vm2.LuaValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LuaUserdata.class)
public class LuaUserdataMixin {
    @Shadow
    public Object m_instance;

    @WrapMethod(method = "checkuserdata(Ljava/lang/Class;)Ljava/lang/Object;")
    public Object silly$backportTexture(Class<?> c, Operation<Object> original) {
        if ((c == FiguraTexture.class || c == Object.class) && this.m_instance instanceof BackportedTexture btex) {
            return btex.getOriginal();
        }

        if ((c == BackportedTexture.class) && this.m_instance instanceof FiguraTexture tex) {
            return BackportedTexture.wrap(tex);
        }

        return original.call(c);
    }
}
