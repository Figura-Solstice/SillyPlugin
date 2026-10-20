package dev.celestial.silly.lua;

//? if >=1.21 {
import net.minecraft.client.DeltaTracker;
//?}
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import dev.celestial.silly.SillyPlugin;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.celestial.silly.SillySettings;
import dev.celestial.silly.SillyUtil;
import dev.celestial.silly.annotations.Alias;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.figuramc.figura.font.Emojis;
import org.figuramc.figura.gui.ActionWheel;
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
import org.figuramc.figura.utils.ClickableTextHelper;
import org.figuramc.figura.utils.LuaUtils;
import org.figuramc.figura.utils.MathUtils;
import org.figuramc.figura.utils.TextUtils;
import org.figuramc.figura.utils.ui.UIHelper;
import org.joml.*;
import org.luaj.vm2.LuaError;

import java.lang.Math;
import java.util.*;

@LuaWhitelist
@LuaTypeDoc(name = "LuaGraphicsAPI", value = "silly.lua_graphics")
public class LuaGraphicsAPI implements AutoCloseable {
    private final GuiGraphics graphics;
    private Deque<Vector4i> tintStack = new ArrayDeque<>();
    private int pushedPoses = 0;
    private int scissors = 0;
    private static Boolean debug = false;
    private boolean valid = true;
    private Vector4f originalColor;
    public LuaGraphicsAPI(GuiGraphics graphics) {
        this.graphics = graphics;
        if (SillyUtil.DEV_MODE)
            debug = SillySettings.LUAGRAPHICS_DEBUG.getBool();
        var shaderColor = RenderSystem.getShaderColor();
        originalColor = new Vector4f(shaderColor[0], shaderColor[1], shaderColor[2], shaderColor[3]);
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
        if (!valid) throw new IllegalStateException("LuaGraphicsAPI cannot be exited multiple times!");

        if (pushedPoses > 0)
            SillyPlugin.LOGGER.warn("LuaGraphicsAPI: exited with {} unpopped poses", pushedPoses);
        while (pushedPoses > 0) {
            graphics.pose().popPose();
            pushedPoses--;
        }
        if (scissors > 0)
            SillyPlugin.LOGGER.warn("LuaGraphicsAPI: exited with {} enabled scissors", scissors);
        while (scissors > 0) {
            graphics.disableScissor();
            scissors--;
        }
        setColor(originalColor);

        valid = false;
    }

    private void setColor(Vector4f col) {
        graphics.flush();
        RenderSystem.setShaderColor(col.x, col.y, col.z, col.w);
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
                        returnType = LuaGraphicsAPI.class
                ),
                @LuaMethodOverload(
                        argumentNames = {"x", "y", "z", "w"},
                        argumentTypes = { Integer.class, Integer.class, Integer.class, Integer.class },
                        returnType = LuaGraphicsAPI.class
                ),
                @LuaMethodOverload(
                        argumentNames = {},
                        argumentTypes = {},
                        returnType = LuaGraphicsAPI.class
                )
        }
    )
    public LuaGraphicsAPI setScissors(Object x, Integer y, Integer z, Integer w) {
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

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.lua_graphics.blit",
            overloads = {
                    @LuaMethodOverload(
                            argumentNames = { "texture", "pos", "region" },
                            argumentTypes = { FiguraTexture.class, FiguraVec2.class, FiguraVec4.class },
                            returnType = LuaGraphicsAPI.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "texture", "pos" },
                            argumentTypes = { FiguraTexture.class, FiguraVec2.class },
                            returnType = LuaGraphicsAPI.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "resourceLocation", "pos", "region" },
                            argumentTypes = { String.class, FiguraVec2.class, FiguraVec4.class },
                            returnType = LuaGraphicsAPI.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "resourceLocation", "pos" },
                            argumentTypes = { String.class, FiguraVec2.class },
                            returnType = LuaGraphicsAPI.class
                    )
            }
    )
    public LuaGraphicsAPI blit(@LuaNotNil Object texture, @LuaNotNil FiguraVec2 pos, FiguraVec4 region) {
        _ensureValid();

        if (texture instanceof FiguraTexture tx) {
            region = region != null ? region : FiguraVec4.of(0,0,tx.getWidth(), tx.getHeight());
            //? if >=1.21.2 {
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
            //?} else {
/*//            innerBlit(
//                    tx.getLocation(),
//                    (int)pos.x, (int)pos.y,
//                    (int)region.z, (int)region.w,
//                    0,
//                    (float)region.x / tx.getWidth(),(float)region.y / tx.getHeight(),(float)region.z / tx.getWidth(),(float)region.w / tx.getHeight()
//            );
            int width = tx.getWidth();
            int height = tx.getHeight();
            RenderSystem.enableBlend();
            graphics.blit(
                    tx.getLocation(),
                    (int)pos.x,
                    (int)pos.y,
                    0,
                    (int)region.x,
                    (int)region.y,
                    (int)region.z,
                    (int)region.w,
                    width,
                    height
            );
            RenderSystem.disableBlend();
//            innerBlit(
//                    tx.getLocation(),
//                    (int)pos.x,
//                    (int)(pos.x + region.z),
//                    (int)pos.y,
//                    (int)(pos.y + region.w),
//                    0,
//                    (float)(region.x / width),
//                    (float)((region.x + region.z) / width),
//                    (float)(region.y / height),
//                    (float)((region.y + region.w) / height)
//            );
            *///?}
        } else if (texture instanceof String str) {
            //? if >=1.21 {
            ResourceLocation loc = ResourceLocation.tryParse(str);
            //?} else {
            /*ResourceLocation loc = new ResourceLocation(str);
            *///?}

            FiguraVec2 size = cachedTextureSizes.computeIfAbsent(loc, (l) -> {
                var s = new FiguraVec2();
                var img = SillyUtil.getImage(l);
                s.x = img.getWidth();
                s.y = img.getHeight();
                img.close();
                return s;
            });
            region = region != null ? region : FiguraVec4.of(0,0,size.x, size.y);

            //? if >=1.21.2 {
            graphics.blit(RenderType::guiTextured, loc, (int)pos.x, (int)pos.y, (int)region.x, (int)region.y, (int)region.z, (int)region.w,(int)size.x,(int)size.y);
            //?} else {
/*//            float width = (float)size.x;
//            float height = (float)size.y;
//            innerBlit(
//                    loc,
//                    (int)pos.x,
//                    (int)(pos.x + region.z),
//                    (int)pos.y,
//                    (int)(pos.y + region.w),
//                    0,
//                    (float)(region.x / width),
//                    (float)((region.x + region.z) / width),
//                    (float)(region.y / height),
//                    (float)((region.y + region.w) / height)
//            );
            RenderSystem.enableBlend();
            graphics.blit(loc, (int)pos.x, (int)pos.y, 0, (int)region.x, (int)region.y, (int)region.z, (int)region.w, (int)size.x,(int)size.y);
            RenderSystem.disableBlend();
            *///?}

        } else {
            throw new LuaError("LuaGraphics.blit, expected FiguraTexture or String, got " + texture.getClass().getSimpleName());
        }

        return this;
    }


    public static Map<ResourceLocation, FiguraVec2> cachedTextureSizes = new HashMap<>();

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.lua_graphics.blit_string",
            overloads = {
                    @LuaMethodOverload(
                            argumentNames = { "string", "pos", "width" },
                            argumentTypes = { String.class, FiguraVec2.class, Integer.class },
                            returnType = LuaGraphicsAPI.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "string", "pos" },
                            argumentTypes = { String.class, FiguraVec2.class },
                            returnType = LuaGraphicsAPI.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "string", "x", "y", "width" },
                            argumentTypes = { String.class, Integer.class, Integer.class, Integer.class },
                            returnType = LuaGraphicsAPI.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "string", "x", "y" },
                            argumentTypes = { String.class, Integer.class, Integer.class },
                            returnType = LuaGraphicsAPI.class
                    )
            }
    )
    public LuaGraphicsAPI blitString(@LuaNotNil String string, @LuaNotNil Object x, Integer y, Integer width) {
        _ensureValid();
        if (width == null) width = graphics.guiWidth();
        Integer finalX = null;
        Integer finalY = null;
        if (x instanceof FiguraVec2 p) {
            finalX = ((Number)p.x).intValue();
            finalY = ((Number)p.y).intValue();
            if (y != null)
                //noinspection SuspiciousNameCombination
                width = y;
        } else {
            finalX = ((Number)x).intValue();
            finalY = y;
        }
        Component comp = TextUtils.tryParseJson(string);
        Emojis.applyEmojis(comp);

//        comp = TextUtils.formatInBounds(comp, Minecraft.getInstance().font, width, )
        graphics.drawWordWrap(Minecraft.getInstance().font, comp, finalX, finalY, width, 0xFFFFFFFF);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.lua_graphics.blit_string_with_hover",
            overloads = {
                    @LuaMethodOverload(
                            argumentNames = { "string", "pos", "width" },
                            argumentTypes = { String.class, FiguraVec2.class, Integer.class },
                            returnType = LuaGraphicsAPI.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "string", "pos" },
                            argumentTypes = { String.class, FiguraVec2.class },
                            returnType = LuaGraphicsAPI.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "string", "x", "y", "width" },
                            argumentTypes = { String.class, Integer.class, Integer.class, Integer.class },
                            returnType = LuaGraphicsAPI.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "string", "x", "y" },
                            argumentTypes = { String.class, Integer.class, Integer.class },
                            returnType = LuaGraphicsAPI.class
                    )
            }
    )
    public LuaGraphicsAPI blitStringWithHover(@LuaNotNil String string, @LuaNotNil Object x, Integer y, Integer width) {
        if (width == null) width = graphics.guiWidth();
        Integer finalX = null;
        Integer finalY = null;
        if (x instanceof FiguraVec2 p) {
            finalX = ((Number)p.x).intValue();
            finalY = ((Number)p.y).intValue();
            if (y != null)
                //noinspection SuspiciousNameCombination
                width = y;
        } else {
            finalX = ((Number)x).intValue();
            finalY = y;
        }
        Component comp = TextUtils.tryParseJson(string);
        Emojis.applyEmojis(comp);

        graphics.drawWordWrap(Minecraft.getInstance().font, comp, finalX, finalY, width, 0xFFFFFFFF);
        var helper = new ClickableTextHelper();
        helper.setMessage(comp);
        var mc = Minecraft.getInstance();
        var maus = mc.mouseHandler;
        if (maus.isMouseGrabbed()) return this;
        int mouseX = (int)(
                maus.xpos()
                        * (double)mc.getWindow().getGuiScaledWidth()
                        / (double)mc.getWindow().getScreenWidth()
        );
        int mouseY = (int)(
                maus.ypos()
                        * (double)mc.getWindow().getGuiScaledHeight()
                        / (double)mc.getWindow().getScreenHeight()
        );
        helper.update(Minecraft.getInstance().font, width);

        var hov = helper.getHoverTooltip(finalX, finalY, mouseX, mouseY);
        if (hov == null) {
            if (debug) helper.renderDebug(graphics, finalX+1, finalY, mouseX, mouseY);
            hov = helper.getHoverTooltip(finalX+1, finalY, mouseX, mouseY);
        } else {
            if (debug) helper.renderDebug(graphics, finalX, finalY, mouseX, mouseY);
        }

        if (hov != null) {
            graphics.renderComponentHoverEffect(Minecraft.getInstance().font,
                    Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hov)), mouseX, mouseY);
        } else {
            if (debug) helper.renderDebug(graphics, finalX, finalY, mouseX, mouseY);
        }
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.lua_graphics.blit_item",
            overloads = {
                    @LuaMethodOverload(
                            argumentNames = { "stackString", "pos" },
                            argumentTypes = { String.class, FiguraVec2.class },
                            returnType = LuaGraphicsAPI.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "stackString", "x", "y" },
                            argumentTypes = { String.class, Integer.class, Integer.class },
                            returnType = LuaGraphicsAPI.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "itemStack", "pos" },
                            argumentTypes = { ItemStackAPI.class, FiguraVec2.class },
                            returnType = LuaGraphicsAPI.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "itemStack", "x", "y" },
                            argumentTypes = { ItemStackAPI.class, Integer.class, Integer.class },
                            returnType = LuaGraphicsAPI.class
                    )
            }
    )
    public LuaGraphicsAPI blitItem(@LuaNotNil Object string, @LuaNotNil Object x, Integer y) {
        _ensureValid();
        FiguraVec2 pos = LuaUtils.parseVec2("blitItem", x, y);
        ItemStack stack = LuaUtils.parseItemStack("blitItem", string);
        graphics.renderFakeItem(stack, (int)pos.x, (int)pos.y);
        return this;
    }

    private Vector4f flattenTintStack() {
        var ret = new Vector4f(1,1,1,1);
        for (Iterator<Vector4i> it = tintStack.descendingIterator(); it.hasNext(); ) {
            var value = it.next();
            ret = ret.mul(new Vector4f(value.x / 255f, value.y / 255f, value.z/255f, value.w/255f));
        }
        return ret;
    }

    // TODO: is it possible to inject into GuiGraphics and tint the underlying vertices instead?
    //       would that provide a more robust solution?
    @SuppressWarnings("MathClampMigration") // stonecutter moment
    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.lua_graphics.push_tint",
            overloads = {
                    @LuaMethodOverload(
                            argumentNames = { "col" },
                            argumentTypes = { FiguraVec4.class },
                            returnType = LuaGraphicsAPI.class
                    )
            }
    )
    public LuaGraphicsAPI pushTint(@LuaNotNil FiguraVec4 col) {
        _ensureValid();
        Vector4i colorInt = new Vector4i((int) (col.x * 255), (int) (col.y * 255), (int) (col.z * 255), (int) (col.w * 255));
//        if (colorInt.x() > 255 || colorInt.y() > 255 || colorInt.z() > 255 || colorInt.w() > 255 | colorInt.x() < 0 || colorInt.y() < 0 || colorInt.z() < 0 | colorInt.w() < 0)
//            throw new LuaError("Invalid color: " + colorInt.toString());
//        int finalInt = colorInt.z << 24 | colorInt.x << 16 | colorInt.y << 8 | colorInt.z;
        colorInt.x = Math.min(Math.max(colorInt.x, 0), 255);
        colorInt.y = Math.min(Math.max(colorInt.y, 0), 255);
        colorInt.z = Math.min(Math.max(colorInt.z, 0), 255);
        colorInt.w = Math.min(Math.max(colorInt.w, 0), 255);
        tintStack.push(colorInt);
        var flattened = flattenTintStack();
        setColor(flattened);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(value = "silly.lua_graphics.pop_tint",
            overloads =
                    {@LuaMethodOverload(returnType = LuaGraphicsAPI.class)}
    )
    public LuaGraphicsAPI popTint() {
        _ensureValid();
        if (tintStack.isEmpty()) throw new LuaError("Tried to pop tint, but it was empty! (mismatched pop/push?)");
        tintStack.pop();
        var flattened = flattenTintStack();
        setColor(flattened);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.lua_graphics.push_pose_matrix",
            overloads = {
                    @LuaMethodOverload(
                            argumentNames = { "matrix" },
                            argumentTypes = { FiguraMat4.class },
                            returnType = LuaGraphicsAPI.class
                    )
            },
            aliases = { "pushPose" }
    )
    public LuaGraphicsAPI pushPoseMatrix(FiguraMat4 matrix) {
        _ensureValid();
        _pushPose();

        //? if >=1.21 {
        graphics.pose().mulPose(matrix.toMatrix4f());
        //?} else {
        /*graphics.pose().mulPoseMatrix(matrix.toMatrix4f());
        *///?}
        return this;
    }

    @LuaWhitelist @Alias
    public LuaGraphicsAPI pushPose(FiguraMat4 matrix) { return pushPoseMatrix(matrix); }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.lua_graphics.fill",
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = { FiguraVec3.class, FiguraVec4.class },
                            argumentNames = { "color", "region" },
                            returnType = LuaGraphicsAPI.class
                    )
            }
    )
    public LuaGraphicsAPI fill(@LuaNotNil FiguraVec3 color, @LuaNotNil FiguraVec4 region) {
        _ensureValid();
        Vec3i colorInt = new Vec3i((int) (color.x * 255), (int) (color.y * 255), (int) (color.z * 255));
        if (colorInt.getX() > 255 || colorInt.getY() > 255 || colorInt.getZ() > 255 | colorInt.getX() < 0 || colorInt.getY() < 0 || colorInt.getZ() < 0)
            throw new LuaError("Invalid color: " + colorInt.toShortString());
        graphics.fill((int) region.x, (int) region.y, (int) (region.z + region.x), (int) (region.w + region.y), 0xFF000000 | (colorInt.getX() << 16) | (colorInt.getY() << 8) | (colorInt.getZ()));
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(value = "silly.lua_graphics.pop_pose",
            overloads =
                    {@LuaMethodOverload(returnType = LuaGraphicsAPI.class)}
    )
    public LuaGraphicsAPI popPose() {
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
                            returnType = LuaGraphicsAPI.class
                    )
            }
    )
    public LuaGraphicsAPI blitEntity(@LuaNotNil LivingEntityAPI<? extends LivingEntity> entity, @LuaNotNil FiguraVec2 pos, @LuaNotNil FiguraVec3 rot) {
        _ensureValid();
        FiguraVec3 rot2 = rot.copy();
        rot2.scale(MathUtils.DEG_TO_RAD);
        Quaternionf quat = new Quaternionf().rotationZYX((float)rot2.x, (float)rot2.y, (float)rot2.z);
        graphics.pose().pushPose();
        graphics.pose().translate(pos.x, pos.y, 0);
        graphics.pose().scale(2,2,2);
        graphics.pose().pushPose();
        graphics.pose().scale(16,16,16);
        graphics.pose().pushPose();
        graphics.pose().mulPose(new Quaternionf().rotationZ(Mth.PI));

        // poked around in the code and supposedly this hides the nameplate.
        UIHelper.paperdoll = true;
        //? if >=1.20.4 {
        InventoryScreen.renderEntityInInventory(graphics, 0, 0, 1, new Vector3f(), quat, null, entity.getEntity());
        //?} else {
        /*InventoryScreen.renderEntityInInventory(graphics, 0, 0, 1, quat, null, entity.getEntity());
        *///?}
        UIHelper.paperdoll = false;

        graphics.pose().popPose();
        graphics.pose().popPose();
        graphics.pose().popPose();
        return this;
    }

    @Override
    public void close() throws Exception {
        exit();
    }
}
