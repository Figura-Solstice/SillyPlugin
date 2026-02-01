package dev.celestial.silly.mixin;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.celestial.silly.SillyPlugin;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;


@Mixin(Entity.class)
public class EntityMixin {
    @WrapOperation(method = "move", at= @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Entity;noPhysics:Z", opcode = Opcodes.GETFIELD))
    public boolean moveMixin(Entity instance, Operation<Boolean> original) {
        return original.call(instance) || SillyPlugin.shouldNoclip(instance);
    }
}
