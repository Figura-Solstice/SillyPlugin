package dev.celestial.silly.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.context.CommandContext;
import dev.celestial.silly.helper.CallerContext;
import dev.celestial.silly.lua.BackportsAPI;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.commands.FiguraCommands;
import org.figuramc.figura.lua.FiguraLuaRuntime;
import org.figuramc.figura.utils.FiguraClientCommandSource;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "org.figuramc.figura.commands.RunCommand", remap = false)
public class FiguraRunCommandMixin {
    @WrapMethod(method = "executeCode")
    private static int executeCodeMixin(CommandContext<FiguraClientCommandSource> context, Operation<Integer> original) {
        Avatar avatar = AvatarManager.getLoadedAvatar(FiguraMod.getLocalPlayerUUID());
        if (avatar != null)
            try(CallerContext ctx = BackportsAPI.openCallerContext(avatar.owner, "run_command")) {
                return original.call(context);
            }
        else
            return original.call(context);
    }
}
