package dev.celestial.silly.helper;

//? if >=1.21 {
import net.minecraft.client.DeltaTracker;
//?}
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.figuramc.figura.font.Emojis;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.api.entity.LivingEntityAPI;
import org.figuramc.figura.math.matrix.FiguraMat4;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.model.rendering.texture.FiguraTexture;
import org.figuramc.figura.model.rendertasks.TextTask;
import org.figuramc.figura.utils.LuaUtils;
import org.figuramc.figura.utils.MathUtils;
import org.figuramc.figura.utils.TextUtils;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.luaj.vm2.LuaError;

@LuaWhitelist
public class LuaGraphics {
    private final GuiGraphics graphics;
    private int pushedPoses = 0;
    private int scissors = 0;
    public LuaGraphics(GuiGraphics graphics) {
        this.graphics = graphics;
    }

    public void _pushPose() {
        graphics.pose().pushPose();
        pushedPoses++;
    }

    public void _popPose() {
        if (pushedPoses <= 0)
            throw new LuaError("Tried to pop pose when no poses were pushed! (mismatched pop/push?)");
        graphics.pose().popPose();
        pushedPoses--;
    }

    public void exit() {
        while (pushedPoses > 0) {
            graphics.pose().popPose();
            pushedPoses--;
        }
        while (scissors > 0) {
            graphics.disableScissor();
            scissors--;
        }
    }

    @LuaWhitelist
    public LuaGraphics setScissors(Object x, int y, int z, int w) {
        FiguraVec4 v = LuaUtils.parseVec4("setScissors", x, y, z, w, 0, 0, 0, 0);
        if (v.length() == 0) {
            scissors--;
            graphics.disableScissor();
            return this;
        }
        graphics.enableScissor((int)v.x, (int)v.y, (int)v.z, (int)v.w);
        scissors++;
        return this;
    }

//    @LuaWhitelist
//    public LuaGraphics blit(@LuaNotNil Object texture, @LuaNotNil int x, @LuaNotNil int y, @LuaNotNil int w, @LuaNotNil int h, Integer u, Integer v, Integer uw, Integer uh) {
//        if (texture instanceof FiguraTexture tx) {
//            return blit(tx.getLocation(), x, y, w, h);
//        } else if (texture instanceof String loc) {
//            return blit(ResourceLocation.read(loc).result().orElse(new ResourceLocation("missing")));
//        }
//        graphics.blit(texture, x, y, 0, (float)0, (float)0, w, h, texture.getWidth(), texture.getHeight());
//        return this;
//    }
    @LuaWhitelist
    public LuaGraphics blit(@LuaNotNil Object texture, @LuaNotNil FiguraVec2 pos, @LuaNotNil FiguraVec2 dims, FiguraVec4 region) {
        if (texture instanceof FiguraTexture tx) {
            region = region != null ? region : FiguraVec4.of(0,0,tx.getWidth(), tx.getHeight());
            //? if >=1.21.2 {
            graphics.blit(RenderType::guiTextured, tx.getLocation(), (int)pos.x, (int)pos.y, 0, (int)region.x, (int)region.y, (int)region.z, (int)region.w, tx.getWidth(), tx.getHeight());
            //?} else {
            /*graphics.blit(tx.getLocation(), (int)pos.x, (int)pos.y, 0, (int)region.x, (int)region.y, (int)region.z, (int)region.w, tx.getWidth(), tx.getHeight());
            *///?}
        } else if (texture instanceof String str) {
            //? if >=1.21 {
            ResourceLocation loc = ResourceLocation.tryParse(str);
            //?} else {
            /*ResourceLocation loc = new ResourceLocation(str);
            *///?}

            AbstractTexture tx = Minecraft.getInstance().getTextureManager().getTexture(loc);
            //? if >=1.21.2 {
            graphics.blit(RenderType::guiTextured, loc, (int)pos.x, (int)pos.y, 0, (int)region.x, (int)region.y, (int)region.z, (int)region.w, 256, 256);
            //?} else {
            /*graphics.blit(loc, (int)pos.x, (int)pos.y, 0, (int)region.x, (int)region.y, (int)region.z, (int)region.w, 256, 256);
            *///?}

        } else {
            throw new LuaError("LuaGraphics.blit 2, expected FiguraTexture or String, got " + texture.getClass().getSimpleName());
        }

        return this;
    }

    @LuaWhitelist
    public LuaGraphics blitString(@LuaNotNil String string, @LuaNotNil Object x, Integer y, Integer width) {
        if (width == null) width = graphics.guiWidth();
        FiguraVec2 pos = LuaUtils.parseVec2("blitString", x, y);
        Component comp = TextUtils.tryParseJson(string);
        Emojis.applyEmojis(comp);

//        comp = TextUtils.formatInBounds(comp, Minecraft.getInstance().font, width, )
        graphics.drawWordWrap(Minecraft.getInstance().font, comp, (int)pos.x, (int)pos.y, width, 0xFFFFFFFF);
        return this;
    }

    @LuaWhitelist
    public LuaGraphics blitItem(@LuaNotNil String string, @LuaNotNil Object x, int y) {
        FiguraVec2 pos = LuaUtils.parseVec2("blitItem", x, y);
        ItemStack stack = LuaUtils.parseItemStack("blitItem", string);
        graphics.renderFakeItem(stack, (int)pos.x, (int)pos.y);
        return this;
    }

    @LuaWhitelist
    public LuaGraphics pushPoseMatrix(FiguraMat4 matrix) {
        _pushPose();

        //? if >=1.21 {
        graphics.pose().mulPose(matrix.toMatrix4f());
        //?} else {
        /*graphics.pose().mulPoseMatrix(matrix.toMatrix4f());
        *///?}
        return this;
    }

    @LuaWhitelist
    public LuaGraphics popPose() {
        _popPose();
        return this;
    }

    @LuaWhitelist
    public LuaGraphics blitEntity(LivingEntityAPI<? extends LivingEntity> entity, FiguraVec2 pos, FiguraVec3 rot) {
        FiguraVec3 rot2 = rot.copy();
        rot2.scale(MathUtils.DEG_TO_RAD);
        Quaternionf quat = new Quaternionf().rotationZYX((float)rot2.x, (float)rot2.y, (float)rot2.z);
        graphics.pose().pushPose();
        graphics.pose().scale(16,16,16);
        //? if >=1.21 {
        InventoryScreen.renderEntityInInventory(graphics, (int)pos.x, (int)pos.y, 1, new Vector3f(), quat, null, entity.getEntity());
        //?} else {
        /*InventoryScreen.renderEntityInInventory(graphics, (int)pos.x, (int)pos.y, 1, quat, null, entity.getEntity());
        *///?}

        graphics.pose().popPose();
        return this;
    }
}
