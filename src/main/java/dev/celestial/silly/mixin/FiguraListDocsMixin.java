package dev.celestial.silly.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.celestial.silly.SillyEnums;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.lua.docs.FiguraDoc;
import org.figuramc.figura.lua.docs.FiguraListDocs;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.FiguraClientCommandSource;
import org.figuramc.figura.utils.FiguraText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mixin(value = FiguraListDocs.class, remap = false)
public class FiguraListDocsMixin {
    @Unique
    private static LiteralArgumentBuilder<FiguraClientCommandSource> silly$generateCommand() {
        Class<SillyEnums.GUI_ELEMENT> en = SillyEnums.GUI_ELEMENT.class;
        LiteralArgumentBuilder<FiguraClientCommandSource> command = LiteralArgumentBuilder.literal("silly_gui_element");
        Collection<?> get = Arrays.stream(SillyEnums.GUI_ELEMENT.values()).map(SillyEnums.GUI_ELEMENT::name).toList();

        // display everything
        command.executes(context -> {
            Collection<?> coll = get;
            if (coll.isEmpty()) {
                FiguraMod.sendChatMessage(FiguraText.of("docs.enum.empty"));
                return 0;
            }

            MutableComponent text = FiguraDoc.HEADER.copy()
                    .append("\n\n")
                    .append(Component.literal("• ")
                            .append(FiguraText.of("docs.text.description"))
                            .append(":")
                            .withStyle(ColorUtils.Colors.PURPLE.style))
                    .append("\n\t")
                    .append(Component.literal("• ")
                            .append(FiguraText.of("docs.enum." + "silly_gui_element"))
                            .withStyle(ColorUtils.Colors.FIGURA_BLUE.style))
                    .append("\n\n")
                    .append(Component.literal("• ")
                            .append(FiguraText.of("docs.text.entries"))
                            .append(":")
                            .withStyle(ColorUtils.Colors.PURPLE.style));

            int i = 0;
            for (Object o : coll) {
                MutableComponent component;

                if (o instanceof Map.Entry e) {
                    component = Component.literal(e.getKey().toString()).withStyle(ChatFormatting.WHITE);
                    for (String s : (List<String>) e.getValue()) {
                        component.append(Component.literal(" | ").withStyle(ChatFormatting.YELLOW))
                                .append(Component.literal(s).withStyle(ChatFormatting.GRAY));
                    }
                } else {
                    component = Component.literal(o.toString()).withStyle(ChatFormatting.WHITE);
                }

                text.append("\n\t");
                text.append(Component.literal("• ").withStyle(ChatFormatting.YELLOW)).append(component);
                i++;
            }

            FiguraMod.sendChatMessage(text);
            return 1;
        });

        // add collection as child for easy navigation
        Collection<?> coll = get;
        for (Object o : coll) {
            String text = o instanceof Map.Entry e ? e.getKey().toString() : o.toString();
            LiteralArgumentBuilder<FiguraClientCommandSource> entry = LiteralArgumentBuilder.literal(text);
            entry.executes(context -> {
                FiguraMod.sendChatMessage(Component.literal(text).withStyle(ColorUtils.Colors.AWESOME_BLUE.style));
                return 1;
            });

            if (o instanceof Map.Entry e) {
                for (String s : (List<String>) e.getValue()) {
                    LiteralArgumentBuilder<FiguraClientCommandSource> child = LiteralArgumentBuilder.literal(s);
                    child.executes(context -> {
                        FiguraMod.sendChatMessage(Component.literal(s).withStyle(ColorUtils.Colors.AWESOME_BLUE.style));
                        return 1;
                    });
                    entry.then(child);
                }
            }

            command.then(entry);
        }

        // return
        return command;
    }
    @Inject(method = "getCommand", at = @At("RETURN"))
    private static void getCommandMixin(CallbackInfoReturnable<LiteralArgumentBuilder<FiguraClientCommandSource>> cir, @Local LiteralArgumentBuilder<FiguraClientCommandSource> root) {
        root.then(silly$generateCommand());

    }
}
