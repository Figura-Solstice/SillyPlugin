package dev.celestial.silly.helper;

//? if >=1.21 {
/*import net.minecraft.client.DeltaTracker;
*///?}
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
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
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
@LuaTypeDoc(name = "LuaGraphicsAPI", value = "silly.lua_graphics")
public class LuaGraphics {
    private final GuiGraphics graphics;
    private int pushedPoses = 0;
    private int scissors = 0;
    private boolean valid = true;
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

    public void _ensureValid() {
        if (!valid) throw new LuaError("Attempted to use a revoked LuaGraphics instance!");
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
        valid = false;
    }

    @LuaWhitelist
    @LuaMethodDoc(value = "silly.lua_graphics.is_revoked")
    public Boolean isRevoked() {
        return !valid;
    }

    @LuaWhitelist
    @LuaMethodDoc(value = "silly.lua_graphics.set_scissors",
        overloads = {
                @LuaMethodOverload(
                        argumentTypes = { FiguraVec4.class },
                        argumentNames = { "region" },
                        returnType = LuaGraphics.class
                ),
                @LuaMethodOverload(
                        argumentNames = {"x", "y", "z", "w"},
                        argumentTypes = { Integer.class, Integer.class, Integer.class, Integer.class },
                        returnType = LuaGraphics.class
                ),
                @LuaMethodOverload(
                        argumentNames = {},
                        argumentTypes = {},
                        returnType = LuaGraphics.class
                )
        }
    )
    public LuaGraphics setScissors(Object x, Integer y, Integer z, Integer w) {
        _ensureValid();
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
    @LuaMethodDoc(
            value = "silly.lua_graphics.blit",
            overloads = {
                    @LuaMethodOverload(
                            argumentNames = { "texture", "pos", "region" },
                            argumentTypes = { FiguraTexture.class, FiguraVec2.class, FiguraVec4.class },
                            returnType = LuaGraphics.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "texture", "pos" },
                            argumentTypes = { FiguraTexture.class, FiguraVec2.class },
                            returnType = LuaGraphics.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "resourceLocation", "pos", "region" },
                            argumentTypes = { String.class, FiguraVec2.class, FiguraVec4.class },
                            returnType = LuaGraphics.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "resourceLocation", "pos" },
                            argumentTypes = { String.class, FiguraVec2.class },
                            returnType = LuaGraphics.class
                    )
            }
    )
    public LuaGraphics blit(@LuaNotNil Object texture, @LuaNotNil FiguraVec2 pos, FiguraVec4 region) {
        _ensureValid();
        if (texture instanceof FiguraTexture tx) {
            region = region != null ? region : FiguraVec4.of(0,0,tx.getWidth(), tx.getHeight());
            //? if >=1.21.2 {
/*//            graphics.blit(
//                    RenderType::guiTextured,
//                    tx.getLocation(),
//                    (int)pos.x,
//                    (int)pos.y,
//                    0,
//                    (int)region.x,
//                    (int)region.y,
//                    (int)region.z,
//                    (int)region.w,
//                    tx.getWidth(),
//                    tx.getHeight(),
//                    tx.getWidth(),
//                    tx.getHeight());
            graphics.blit(
                    RenderType::guiTextured,
                    tx.getLocation(),
                    (int)pos.x,
                    (int)pos.y,
                    (float)region.x,
                    (float)region.y,
                    (int)region.z,
                    (int)region.w,
                    tx.getWidth(),
                    tx.getHeight()
            );
            *///?} else {
            graphics.blit(
                    tx.getLocation(),
                    (int)pos.x,
                    (int)pos.y,
                    0,
                    (int)region.x,
                    (int)region.y,
                    (int)region.z,
                    (int)region.w,
                    tx.getWidth(),
                    tx.getHeight());
            //?}
        } else if (texture instanceof String str) {
            //? if >=1.21 {
            /*ResourceLocation loc = ResourceLocation.tryParse(str);
            *///?} else {
            ResourceLocation loc = new ResourceLocation(str);
            //?}

            //? if >=1.21.2 {
            /*graphics.blit(RenderType::guiTextured, loc, (int)pos.x, (int)pos.y, (int)region.x, (int)region.y, (int)region.z, (int)region.w, 256, 256);
            *///?} else {
            graphics.blit(loc, (int)pos.x, (int)pos.y, 0, (int)region.x, (int)region.y, (int)region.z, (int)region.w, 256, 256);
            //?}

        } else {
            throw new LuaError("LuaGraphics.blit, expected FiguraTexture or String, got " + texture.getClass().getSimpleName());
        }

        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.lua_graphics.blit_string",
            overloads = {
                    @LuaMethodOverload(
                            argumentNames = { "string", "pos", "width" },
                            argumentTypes = { String.class, FiguraVec2.class, Integer.class },
                            returnType = LuaGraphics.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "string", "pos" },
                            argumentTypes = { String.class, FiguraVec2.class },
                            returnType = LuaGraphics.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "string", "x", "y", "width" },
                            argumentTypes = { String.class, Integer.class, Integer.class, Integer.class },
                            returnType = LuaGraphics.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "string", "x", "y" },
                            argumentTypes = { String.class, Integer.class, Integer.class },
                            returnType = LuaGraphics.class
                    )
            }
    )
    public LuaGraphics blitString(@LuaNotNil String string, @LuaNotNil Object x, Integer y, Integer width) {
        _ensureValid();
        if (width == null) width = graphics.guiWidth();
        FiguraVec2 pos;
        if (x instanceof FiguraVec2 p) {
            pos = p;
            width = y;
        } else {
            pos = FiguraVec2.of((Double)x, y);
        }
        Component comp = TextUtils.tryParseJson(string);
        Emojis.applyEmojis(comp);

//        comp = TextUtils.formatInBounds(comp, Minecraft.getInstance().font, width, )
        graphics.drawWordWrap(Minecraft.getInstance().font, comp, (int)pos.x, (int)pos.y, width, 0xFFFFFFFF);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.lua_graphics.blit_item",
            overloads = {
                    @LuaMethodOverload(
                            argumentNames = { "stackString", "pos" },
                            argumentTypes = { String.class, FiguraVec3.class },
                            returnType = LuaGraphics.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "stackString", "x", "y" },
                            argumentTypes = { String.class, Integer.class, Integer.class },
                            returnType = LuaGraphics.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "itemStack", "pos" },
                            argumentTypes = { ItemStackAPI.class, FiguraVec3.class },
                            returnType = LuaGraphics.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "itemStack", "x", "y" },
                            argumentTypes = { ItemStackAPI.class, Integer.class, Integer.class },
                            returnType = LuaGraphics.class
                    )
            }
    )
    public LuaGraphics blitItem(@LuaNotNil Object string, @LuaNotNil Object x, Integer y) {
        _ensureValid();
        FiguraVec2 pos = LuaUtils.parseVec2("blitItem", x, y);
        ItemStack stack = LuaUtils.parseItemStack("blitItem", string);
        graphics.renderFakeItem(stack, (int)pos.x, (int)pos.y);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.lua_graphics.push_pose_matrix",
            overloads = {
                    @LuaMethodOverload(
                            argumentNames = { "matrix" },
                            argumentTypes = { FiguraMat4.class },
                            returnType = LuaGraphics.class
                    )
            }
    )
    public LuaGraphics pushPoseMatrix(FiguraMat4 matrix) {
        _ensureValid();
        _pushPose();

        //? if >=1.21 {
        /*graphics.pose().mulPose(matrix.toMatrix4f());
        *///?} else {
        graphics.pose().mulPoseMatrix(matrix.toMatrix4f());
        //?}
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(value = "silly.lua_graphics.pop_pose",
            overloads =
                    {@LuaMethodOverload(returnType = LuaGraphics.class)}
    )
    public LuaGraphics popPose() {
        _ensureValid();
        _popPose();
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.lua_graphics.blit_entity",
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = { LivingEntityAPI.class, FiguraVec2.class, FiguraVec3.class },
                            argumentNames = { "entity", "pos", "rot" },
                            returnType = LuaGraphics.class
                    )
            }
    )
    public LuaGraphics blitEntity(@LuaNotNil LivingEntityAPI<? extends LivingEntity> entity, @LuaNotNil FiguraVec2 pos, @LuaNotNil FiguraVec3 rot) {
        _ensureValid();
        FiguraVec3 rot2 = rot.copy();
        rot2.scale(MathUtils.DEG_TO_RAD);
        Quaternionf quat = new Quaternionf().rotationZYX((float)rot2.x, (float)rot2.y, (float)rot2.z);
        graphics.pose().pushPose();
        graphics.pose().translate(pos.x, pos.y, 0);
        graphics.pose().scale(2,-2,2);
        graphics.pose().pushPose();
        graphics.pose().scale(16,16,16);
        //? if >=1.21 {
        /*InventoryScreen.renderEntityInInventory(graphics, 0, 0, 1, new Vector3f(), quat, null, entity.getEntity());
        *///?} else {
        InventoryScreen.renderEntityInInventory(graphics, 0, 0, 1, quat, null, entity.getEntity());
        //?}

        graphics.pose().popPose();
        graphics.pose().popPose();
        return this;
    }
}
