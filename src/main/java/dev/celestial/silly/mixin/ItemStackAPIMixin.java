package dev.celestial.silly.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.world.item.ItemStack;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ItemStackAPI.class, remap = false)
public class ItemStackAPIMixin {
    @Shadow
    @Final
    public ItemStack itemStack;

    //? if >=1.20.5 {
    @WrapMethod(method = "toStackString")
    public String silly$fixStackString(Operation<String> original) {
        var stack = this.itemStack;
        ItemInput input = new ItemInput(stack.getItemHolder(), stack.getComponentsPatch());
        assert Minecraft.getInstance().player != null;
        return input.serialize(Minecraft.getInstance().player.registryAccess());
    }
    //?}
}
