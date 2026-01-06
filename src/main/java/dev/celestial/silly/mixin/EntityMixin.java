package dev.celestial.silly.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.celestial.silly.SillyPlugin;
import net.minecraft.world.entity.Entity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(Entity.class)
public class EntityMixin {
    @WrapOperation(method = "move", at= @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Entity;noPhysics:Z", opcode = Opcodes.GETFIELD))
    public boolean moveMixin(Entity instance, Operation<Boolean> original) {
        return original.call(instance) || SillyPlugin.shouldNoclip();
    }
}
