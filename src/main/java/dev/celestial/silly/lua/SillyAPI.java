package dev.celestial.silly.lua;

//? if >=1.21.1 {
import com.mojang.authlib.yggdrasil.ProfileResult;
//?} else {
/*import com.mojang.authlib.GameProfile;
*///?}
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.brigadier.StringReader;
import com.pngencoder.PngEncoder;
import dev.celestial.silly.*;
import dev.celestial.silly.annotations.*;
import dev.celestial.silly.helper.*;
import dev.celestial.silly.helper.window.ActiveWindowFetcher;
import dev.celestial.silly.helper.window.NullWindowFetcher;
import dev.celestial.silly.mixin.AvatarAccessor;
import dev.celestial.silly.mixin.LuaScriptParserInvokers;
import dev.celestial.silly.mixin.RuntimeAccessor;
import dev.celestial.silly.not_a_mixin.AvatarExtensions;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTagVisitor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.avatar.local.LocalAvatarFetcher;
import org.figuramc.figura.backend2.NetworkStuff;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.font.Emojis;
import org.figuramc.figura.gui.widgets.lists.AvatarList;
import org.figuramc.figura.lua.*;
import org.figuramc.figura.lua.api.ClientAPI;
import org.figuramc.figura.lua.api.entity.PlayerAPI;
import org.figuramc.figura.lua.api.ping.PingFunction;
import org.figuramc.figura.lua.api.world.BlockStateAPI;
import org.figuramc.figura.lua.api.world.WorldAPI;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.matrix.FiguraMat4;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.mixin.input.KeyMappingAccessor;
import org.figuramc.figura.model.rendering.texture.FiguraTexture;
import org.figuramc.figura.parsers.LuaScriptParser;
import org.figuramc.figura.permissions.PermissionManager;
import org.figuramc.figura.permissions.PermissionPack;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.EntityUtils;
import org.figuramc.figura.utils.LuaUtils;
import org.figuramc.figura.utils.TextUtils;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.*;
import org.luaj.vm2.ast.Chunk;
import org.luaj.vm2.ast.NameResolver;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.parser.LuaParser;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@LuaWhitelist
@LuaTypeDoc(name = "SillyAPI", value = "silly")
@AutoPropertyWhitelist
public class SillyAPI {
    public final Avatar avatar;
    public final Minecraft minecraft;
    public final FiguraLuaRuntime runtime;
    public Overridable<Boolean> mayFly = new Overridable<>();
    public Overridable<Boolean> gravity = new Overridable<>();
    public Overridable<Boolean> friction = new Overridable<>();
    public Overridable<Float> frictionValue = new Overridable<>();
    public Overridable<FiguraMat4> guiMat = new Overridable<>();
    public boolean disableEntityCollisions = false;
    public boolean noclip = false;
    public boolean local;
    public Set<SillyEnums.GUI_ELEMENT> disabledElements = new HashSet<>();
    public boolean fakeBlocksDisabled = false;
    public Set<UUID> customSubscriptions = new HashSet<>();
    public Set<String> hiddenPlayers = new HashSet<>();

    public SillyAPI(FiguraLuaRuntime runtime) {
        this.avatar = runtime.owner;
        this.runtime = runtime;
        this.minecraft = Minecraft.getInstance();
        this.backports = new BackportsAPI(runtime);
        this.compats = new SillyCompatsAPI(runtime);
        local = avatar.isHost;
        ((AvatarExtensions)avatar).silly$setProfiler(profiler);
        if (local) SillyPlugin.hostInstance = this;
    }

    @LuaWhitelist
    public SillyAPI setPlayerVisible(@LuaNotNil String player, Boolean visible) {
        if (visible) hiddenPlayers.remove(player);
        else hiddenPlayers.add(player);
        return this;
    }

    @AutoProperty
    @ReadOnly
    @LuaFieldDoc("silly.compats")
    public SillyCompatsAPI compats;

    @LuaWhitelist
    public Boolean isPlayerVisible() {
        var hostInstance = SillyPlugin.hostInstance;
        if (hostInstance == null) return true;
        return hostInstance.hiddenPlayers.contains(avatar.owner.toString()) || hostInstance.hiddenPlayers.contains(avatar.entityName);
    }

    @LuaFieldDoc("silly.profiler")
    @AutoProperty @ReadOnly public SillyProfiler profiler = new SillyProfiler();

    @LuaFieldDoc("silly.backports")
    @AutoProperty @ReadOnly public BackportsAPI backports;

    @LuaFieldDoc("silly.collection")
    @AutoProperty @ReadOnly public CollectionAPI collection = CollectionAPI.Instance;

    public void onPanic(boolean panic) {
        if (!local) return;
        if (minecraft.player != null) {
            Abilities a = minecraft.player.getAbilities();
            // .getValue() will not be null if .hasValue() is true
            //noinspection DataFlowIssue
            if (a.flying && !a.mayfly && this.mayFly.isOverridden() && this.mayFly.getValue()) {
                a.flying = false;
            }
            if (gravity.isOverridden())
                //noinspection DataFlowIssue
                minecraft.player.setNoGravity(!panic && gravity.getValue());
            if (friction.isOverridden())
                //noinspection DataFlowIssue
                minecraft.player.setDiscardFriction(!panic && friction.getValue());
        }
    }

    public void cleanup() {
        SillyPlugin.LOGGER.info("SillyAPI.cleanup() for {}", avatar.owner);
        ClientLevel level = minecraft.level;
        SillyBlockHandler.removeOf(avatar.owner, level);

        if (!local) return; // START host cleanup
        if (minecraft.player != null) {
            Abilities a = minecraft.player.getAbilities();
            // .getValue() will not be null if .hasValue() is true
            //noinspection DataFlowIssue
            if (a.flying && !a.mayfly && this.mayFly.isOverridden() && this.mayFly.getValue()) {
                a.flying = false;
            }
            if (gravity.isOverridden())
                minecraft.player.setNoGravity(false);
            if (friction.isOverridden())
                minecraft.player.setDiscardFriction(false);
        }
        SillyPlugin.hostInstance = null;
    }

    public void cheatExecutor(Consumer<LocalPlayer> callback) {
        cheatExecutor(callback, true);
    }

    public void cheatExecutor(Consumer<LocalPlayer> callback, boolean mustBeHost) {
        if (!SillySettings.CHEATS.getBool()) return;
        if (mustBeHost && !local) return;
        if (!(minecraft.player instanceof LocalPlayer)) return;
        if (minecraft.gameMode == null) return;

        ClientPacketListener con = minecraft.getConnection();
        if (con == null) return;
        ServerData servDt = con.getServerData();
        Component motd = servDt != null ? servDt.motd : Component.empty();

        // turns out motd can, under some circumstances, be null.
        // i don't know what circumstances, but it can happen.
        //noinspection ReassignedVariable,ConstantValue
        if (motd == null) motd = Component.empty();

        if (!(
                minecraft.player.hasPermissions(2)
                || minecraft.gameMode.getPlayerMode().isCreative()
                || minecraft.isSingleplayer()
                || motd.getString().contains("§s§i§l§l§y§p§l§u§g§i§n")
                // some servers optimize the MOTD by removing
                // formatting codes that do nothing. (COUGH COUGH
                // PAPER).
                || motd.getString().contains("§s§i§y§p§u§g§i")
        ) || (motd.getString().contains("§!§s§i§l§l§y§p§l§u§g§i§n") || motd.getString().contains("§!§s§i§y§p§u§g§i"))) return;
        callback.accept(minecraft.player);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.get_error",
            overloads = {
                    @LuaMethodOverload(
                            argumentNames = { "name" },
                            argumentTypes = { String.class },
                            returnType = Component.class
                    )
            }
    )
    public Component getError(@LuaNotNil String name) {
        Avatar avatar = SillyUtil.getAvatar(name);
        if (avatar == null) return null;
        if (avatar.scriptError)
            return avatar.errorText;
        return null;
    }

    @LuaWhitelist
    public void stopAvatar(String msg) {
        msg = msg != null ? msg : "Avatar stopped execution!";
        avatar.scriptError = true;
        avatar.errorText = TextUtils.tryParseJson(msg).copy().withStyle(ColorUtils.Colors.LUA_ERROR.style);
        avatar.clearParticles();
        avatar.clearSounds();
        avatar.closeBuffers();
        avatar.closeStreams();

        // yoinked from goofy
        ((RuntimeAccessor) runtime).getSetHookFunction().invoke(LuaValue.varargsOf(new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.valueOf(0);
            }
        }, LuaValue.EMPTYSTRING, LuaValue.valueOf(1)));
        avatar.luaRuntime = null;
    }

    @LuaWhitelist
    public Component applyEmojis(@LuaNotNil String text) {
        return Emojis.applyEmojis(TextUtils.tryParseJson(text));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.get_skin",
            overloads = {
                    @LuaMethodOverload(
                            argumentNames = { "player" },
                            argumentTypes = { PlayerAPI.class },
                            returnType = String.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "name", "callback" },
                            argumentTypes = { String.class, LuaFunction.class }
                    )
            }
    )
    public FiguraTexture getSkin(@LuaNotNil Object param, LuaFunction callback) {
        if (param instanceof PlayerAPI plr) {
            //? if >=1.21.1 {
            var skin = ((AbstractClientPlayer)plr.getEntity()).getSkin();
            return avatar.registerTexture("silly.getSkin", SillyUtil.getImage(skin.texture()), true);
             //?} else {
            /*var skin = ((AbstractClientPlayer)plr.getEntity()).getSkinTextureLocation();
            return avatar.registerTexture("silly.getSkin(" + plr.getName() + ")", SillyUtil.getImage(skin), true);
            *///?}
        } else if (param instanceof String name) {
            UUID uuid = SillyUtil.getUUID(name);
            Util.backgroundExecutor().execute(() -> {
                //? if >=1.21.1 {
                ProfileResult profile = minecraft.getMinecraftSessionService().fetchProfile(uuid, false);
                if (profile == null) {
                    minecraft.execute(() -> {
                        if (avatar.luaRuntime == null || avatar.scriptError) return;
                        avatar.luaRuntime.error(new LuaError("Could not find user " + name));
                    });
                    return;
                }
                var skin = minecraft.getSkinManager().getOrLoad(profile.profile());
                skin.thenAcceptAsync(playerSkin -> {
                    //? if >=1.21.4 {
                    playerSkin.ifPresent(sk -> {
                    //?} else {
                        /*var sk = playerSkin;
                        *///?}
                        try (CallerContext ctx = BackportsAPI.openCallerContext(avatar.owner, null, "silly.getSkin.callback")) {
                            if (avatar.luaRuntime == null || avatar.scriptError) return;
                            SillyUtil.logAsAvatar(avatar, SillyPlugin.LOGGER::info, "Loc: " + sk.texture().toString());
                            var tx = avatar.registerTexture("silly.getSkin", SillyUtil.getImage(sk.texture()), true);
                            callback.invoke(runtime.typeManager.javaToLua(tx));
                        } catch (Exception e) {
                            avatar.luaRuntime.error(e);
                        }
                    //? if >=1.21.4 {
                    });
                    //?}

                }, minecraft);
                //?} else {
                    /*GameProfile profile = minecraft.getMinecraftSessionService().fillProfileProperties(new GameProfile(uuid, null), true);
                    var skin = minecraft.getSkinManager().getInsecureSkinLocation(profile);
                    callback.invoke(LuaValue.valueOf(skin.toString()));
                *///?}
            });
            return null;
        } else {
            throw new LuaError("Parameter to getSkin must be either string or PlayerAPI!");
        }
    }

    @LuaWhitelist
    @LuaMethodDoc(value = "silly.get_figura_debug_string")
    public String getFiguraDebugString() {
        // taken from that one mixin in figura
        StringBuilder lines = new StringBuilder();
        lines.append(ChatFormatting.AQUA + "[" + FiguraMod.MOD_NAME + "]" + ChatFormatting.RESET);
        lines.append('\n');
        lines.append("Version: " + FiguraMod.VERSION);
        lines.append('\n');

        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        lines.append(String.format("Model Complexity: %d\n", avatar.complexity.pre));
        lines.append(String.format("Animations Complexity: %d\n", avatar.animationComplexity));
        lines.append(String.format("Animations instructions: %d\n", avatar.animation.pre));
        lines.append(String.format("Init instructions: %d (W: %d E: %d)\n", avatar.init.getTotal(), avatar.init.pre, avatar.init.post));
        lines.append(String.format("Tick instructions: %d (W: %d E: %d)\n", avatar.tick.getTotal() + avatar.worldTick.getTotal(), avatar.worldTick.pre, avatar.tick.pre));
        lines.append(String.format("Render instructions: %d (E: %d PE: %d)\n", avatar.render.getTotal(), avatar.render.pre, avatar.render.post));
        lines.append(String.format("World Render instructions: %d (W: %d PW: %d)\n", avatar.worldRender.getTotal(), avatar.worldRender.pre, avatar.worldRender.post));
        lines.append(String.format("Pings per second: ↑%d, ↓%d\n", NetworkStuff.pingsSent, NetworkStuff.pingsReceived));

        return lines.toString();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.set_gravity",
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = { Boolean.class },
                            argumentNames = { "gravity" },
                            returnType = SillyAPI.class
                    )
            }
    )
    public SillyAPI setGravity(Boolean gravity) {
        cheatExecutor(plr -> {
            plr.setNoGravity((!gravity));
            this.gravity.setValue(!gravity);
        });
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.set_entity_collisions",
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = { Boolean.class },
                            argumentNames = { "collisions" },
                            returnType = SillyAPI.class
                    )
            }
    )
    public SillyAPI setEntityCollisions(Boolean collisions) {
        cheatExecutor(plr -> {
            this.disableEntityCollisions = !collisions;
        });
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(value = "silly.get_version")
    public String getVersion() {
        return SillyPlugin.Loader.getModVersion(SillyPlugin.MOD_ID);
    }

    @LuaWhitelist
    @LuaMethodDoc(value = "silly.set_gui_matrix",
    overloads = {
            @LuaMethodOverload(
                    argumentNames = { "mat" },
                    argumentTypes = { FiguraMat4.class },
                    returnType = SillyAPI.class
            )
    })
    public SillyAPI setGuiMatrix(FiguraMat4 mat) {
        guiMat.setValue(mat);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(value = "silly.set_friction",
        overloads = {
            @LuaMethodOverload(
                    argumentTypes = { Boolean.class },
                    argumentNames = { "friction" },
                    returnType = SillyAPI.class
            ),
            @LuaMethodOverload(
                    argumentTypes = { Float.class },
                    argumentNames = { "friction" },
                    returnType = SillyAPI.class
            )
        }
    )
    public SillyAPI setFriction(Object friction) {
        if (friction instanceof Boolean fric) {
            cheatExecutor(plr -> {
                plr.setDiscardFriction((!fric));
                this.friction.setValue(!fric);
            });
        } else if (friction instanceof Float fric) {
            cheatExecutor(plr -> {
                this.frictionValue.setValue(fric);
            });
        } else if (friction == null) {
            setFriction(true);
            this.friction.setValue(null);
            this.frictionValue.setValue(null);
        } else {
            LuaValue val = avatar.luaRuntime.typeManager.javaToLua(friction).arg1();
            if (val.isboolean())
                return setFriction(val.checkboolean());
            setFriction(val.checknumber().tofloat());
        }
        return this;
    }

    // this is just extura's implementation.
    // my ass would NOT have figured this out
    @LuaWhitelist
    public void setBindPressed(@LuaNotNil String id, boolean state) {
        if (!local) return;
        KeyMapping key = KeyMappingAccessor.getAll().get(id);
        if (key == null)
            throw new LuaError("Failed to find key: \"" + id + "\"");
        key.setDown(state);
        var getBypassedIdiot = (dev.celestial.silly.mixin.KeyMappingAccessor)key;
        getBypassedIdiot.silly$setClickCount(getBypassedIdiot.silly$getClickCount()+1);
    }

    @LuaWhitelist
    public String saveTextureIndexed(@LuaNotNil FiguraTexture texture) {
        NativeImage img = texture.copy();
        BufferedImage img2 = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                //? if >=1.21.4 {
                img2.setRGB(x,y,img.getPixel(x,y));
                 //?} else {
                /*int col = img.getPixelRGBA(x,y);
                int a = (col >> 24) & 0xFF;
                int b = (col >> 16) & 0xFF;
                int g = (col >> 8) & 0xFF;
                int r = col & 0xFF;
                col = (a << 24) | (r << 16) | (g << 8) | b;
                img2.setRGB(x,y,col);
                *///?}
            }
        }
        img.close();
        PngEncoder enc = new PngEncoder();
        var bytes = enc.withBufferedImage(img2)
                .withCompressionLevel(9)
                .withTryIndexedEncoding(true)
                .toBytes();
        return Base64.getEncoder().encodeToString(bytes);
    }

    @LuaWhitelist
    @LuaMethodDoc(value = "silly.get_permissions",
        overloads = {
                @LuaMethodOverload(
                        argumentNames = { "name" },
                        argumentTypes = { String.class },
                        returnType = LuaTable.class
                ),
                @LuaMethodOverload(returnType = LuaTable.class)
        }
    )
    public LuaTable getPermissions(String name) {
        name = name == null ? avatar.owner.toString() : name;
        Avatar avatar = SillyUtil.getAvatar(name);
        if (avatar == null) return null;
        LuaTable ret = new LuaTable();
        PermissionPack pack = avatar.permissions;
        for (var perm : Permissions.DEFAULT) {
            if (perm.isToggle)
                ret.set("figura:" + perm.name.toLowerCase(Locale.ROOT), pack.get(perm) != 0 ? LuaValue.TRUE : LuaValue.FALSE);
            else
                ret.set("figura:" + perm.name.toLowerCase(Locale.ROOT), pack.get(perm));
        }
        for (var perms : PermissionManager.CUSTOM_PERMISSIONS.entrySet()) {
            String id = perms.getKey();
            for (var perm : perms.getValue()) {
                String key = id.toLowerCase(Locale.ROOT) + ":" + perm.name.toLowerCase(Locale.ROOT);
                if (perm.isToggle)
                    ret.set(key, pack.get(perm) != 0 ? LuaValue.TRUE : LuaValue.FALSE);
                else
                    ret.set(key, pack.get(perm));
            }
        }
        return ret;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.set_permission",
            overloads = {
                    @LuaMethodOverload(
                            argumentNames = { "name", "permissionKey", "value" },
                            argumentTypes = { String.class, String.class, Boolean.class }
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "name", "permissionKey", "value" },
                            argumentTypes = { String.class, String.class, Integer.class }
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "name", "permissionKey" },
                            argumentTypes = { String.class, String.class }
                    ),
            }
    )
    public void setPermission(@LuaNotNil String name, @LuaNotNil String permissionKey, Object value) {
        if (!local) return;
//        Avatar avatar = SillyUtil.getAvatar(name);
//        if (avatar == null) return;
        String namespace = "figura";
        if (permissionKey.contains(":")) {
            ResourceLocation awa = ResourceLocation.tryParse(permissionKey);
            if (awa != null) {
                namespace = awa.getNamespace();
                permissionKey = awa.getPath();
            } else throw new LuaError("Invalid permission key: " + permissionKey);
        }
        namespace = namespace.toLowerCase(Locale.ROOT);
        PermissionPack pack = PermissionManager.get(SillyUtil.getUUID(name));

        if (namespace.equals("figura")) {
            for (var perm : Permissions.DEFAULT) {
                if (Objects.equals(perm.name.toUpperCase(), permissionKey.toUpperCase())) {
                    checkPerm(pack, perm, permissionKey, value, FiguraMod.MOD_ID);
                    return;
                }
            }
        } else {
            var perms = PermissionManager.CUSTOM_PERMISSIONS.get(namespace);
            if (perms == null) throw new LuaError("Unknown namespace: " + namespace);
            for (var perm : perms) {
                if (Objects.equals(perm.name.toUpperCase(), permissionKey.toUpperCase())) {
                    checkPerm(pack, perm, permissionKey, value, namespace);
                    return;
                }
            }
        }

        throw new LuaError("Permission " + permissionKey + " not found!");
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.set_permission_category",
            overloads = {
                    @LuaMethodOverload(
                            argumentNames = { "name", "category" },
                            argumentTypes = { String.class, String.class }
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "name" },
                            argumentTypes = { String.class }
                    )
            }
    )
    public void setPermissionCategory(@LuaNotNil String name, String category) {
        if (!local) return;
        Avatar avatar = SillyUtil.getAvatar(name);
        if (avatar == null) return;
        Permissions.Category cat = PermissionManager.getDefaultCategory();
        if (category != null) {
            try {
                cat = Permissions.Category.valueOf(category.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new LuaError("Unknown category: " + category);
            }
        }

        avatar.permissions.setCategory(PermissionManager.CATEGORIES.getOrDefault(cat, new PermissionPack.CategoryPermissionPack(cat)));
    }

    private void checkPerm(PermissionPack pack, Permissions perm, String permissionKey, Object value, String id) {
        if (value == null) {
            pack.reset(perm);
            return;
        }
        if (perm.isToggle) {
            if (value instanceof Integer num) {
                value = num != 0;
            }
            if (value instanceof Boolean bl) {
                pack.insert(perm, bl ? 1 : 0, id);
            } else {
                throw new LuaError("Expected value to a boolean permission to be a boolean!");
            }
        } else {
            if (value instanceof Integer num) {
                pack.insert(perm, num, id);
            } else {
                throw new LuaError("Expected value to a number permission to be a number!");
            }
        }
    }

    @LuaWhitelist
    @LuaMethodDoc("silly.update_avatar_size")
    public void updateAvatarSize() {
        if (local)
            avatar.fileSize = ((AvatarAccessor)avatar).silly$getFileSize();
    }



    @LuaWhitelist
    @LuaMethodDoc("silly.cat")
    public void cat() {
        if (!local) return;
        if (!Configs.CHAT_MESSAGES.value) return;
        ClientPacketListener con = minecraft.getConnection();
        if (con == null) return;
        con.sendChat("meow");
    }

    @LuaWhitelist
    @LuaMethodDoc("silly.what_does_bumpscocity_do")
    public String whatDoesBumpscocityDo() {
        throw new LuaError("");
    }

    @LuaWhitelist
    @LuaMethodDoc("silly.get_bumpscocity")
    public Integer getBumpscocity() {
        int value = avatar.permissions.get(SillyPermissions.BUMPSCOCITY);
        if (value > 1000) {
            throw new LuaError("Dear god, this is way too much bumpscocity! (1000 max)");
        }
        return value;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.set_hud_element_visible",
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = {SillyEnums.GUI_ELEMENT.class, Boolean.class},
                            argumentNames = { "element", "state" },
                            returnType = SillyAPI.class
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {LuaTable.class, Boolean.class},
                            argumentNames = { "elements", "state" },
                            returnType = SillyAPI.class
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {SillyEnums.GUI_ELEMENT.class},
                            argumentNames = { "element" },
                            returnType = SillyAPI.class
                    )
            },
            aliases = { "setRenderHudElement", "setHudElementDisabled" }
    )
    public SillyAPI setHudElementVisible(@LuaNotNil Object elements, Boolean state) {
        if (!local) return this;
        if (elements instanceof LuaTable tbl) {
            for (int i = 1; i < tbl.length()+1; i++) {
                setHudElementVisible(tbl.get(i), state);
            }
        } else if (elements instanceof String element) {
            SillyEnums.GUI_ELEMENT el = SillyEnums.GUI_ELEMENT.valueOf(element);
            if (state == null) state = disabledElements.contains(el);
            if (state) {
                disabledElements.remove(el);
            } else {
                disabledElements.add(el);
            }
        } else {
            throw new LuaError("Expected list or string for first argument, received " + elements.getClass().getSimpleName());
        }
        return this;
    }

    @LuaWhitelist
    @Alias
    public SillyAPI setRenderHudElement(@LuaNotNil Object element, Boolean state) {
        return setHudElementVisible(element, state);
    }

    // cosmic your oopsie is now canon
    @LuaWhitelist
    @Alias
    public SillyAPI setHudElementDisabled(@LuaNotNil Object element, Boolean state) {
        return setHudElementVisible(element, !state);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.set_noclip",
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = { Boolean.class },
                            argumentNames = { "state" }
                    )
            }
    )
    public void setNoclip(@LuaNotNil Boolean state) {
        cheatExecutor(localPlayer -> {
            noclip = state;
        });
    }

    @LuaWhitelist
    public void clearFakeBlocks() {
        SillyBlockHandler.removeOf(avatar.owner, minecraft.level);
    }

    @LuaWhitelist
    @Unfinished
    public LuaValue[] getFocusedWindow(String override) {
        if (!local) return new LuaValue[] {
            ActiveWindowFetcher.NULL.toTable(),
            LuaValue.valueOf(NullWindowFetcher.class.getSimpleName())
        };
        ActiveWindowFetcher fetcher;
        if (override != null) {
            var clazz = ActiveWindowFetcher.FETCHERS.get(override);
            if (clazz != null) {
                try {
                    fetcher = clazz.getConstructor().newInstance();
                } catch (Exception e) {
                    fetcher = ActiveWindowFetcher.findFetcher();
                }
            } else {
                fetcher = ActiveWindowFetcher.findFetcher();
            }
        } else {
            fetcher = ActiveWindowFetcher.findFetcher();
        }
        ActiveWindowFetcher.WindowInfo value;
        try {
            value = fetcher.getWindow();
        } catch (Exception e) {
            SillyUtil.Devlog(e.getMessage());
            value = ActiveWindowFetcher.NULL;
        }
        return new LuaValue[] {
            value.toTable(),
            LuaValue.valueOf(fetcher.getClass().getSimpleName())
        };
    }

    private void setBlockInternal(BlockPos pos, BlockState state) {
        setBlockInternal(pos, state, null);
    }

    private void setBlockInternal(BlockPos pos, BlockState state, @Nullable CompoundTag entity) {
        cheatExecutor(plr -> {
            if (avatar.permissions.get(SillyPermissions.FAKE_BLOCKS) != 1) {
                avatar.noPermissions.add(SillyPermissions.FAKE_BLOCKS);
                return;
            } else {
                avatar.noPermissions.remove(SillyPermissions.FAKE_BLOCKS);
            }
            if (minecraft.level != null && minecraft.level.isClientSide) {
                ClientLevel lvl = minecraft.level;
                SillyBlockHandler.REAL_BLOCKS.computeIfAbsent(pos, k -> {
                    BlockState realstate = lvl.getBlockState(k);
                    var realentity = lvl.getBlockEntity(k);
                    return new SillyBlockContainer(k, realstate, realentity);
                });
                var cont = new SillyBlockContainer(pos, state, avatar.owner);
                SillyBlockHandler.BLOCKS.put(pos, cont);

                if (!(SillyPlugin.hostInstance != null && SillyPlugin.hostInstance.fakeBlocksDisabled)) {
                    SillyBlockHandler.setBlock(cont, lvl);
                    if (entity != null && state.getBlock() instanceof EntityBlock) {
                        var blockEnt = lvl.getBlockEntity(pos);
                        if (blockEnt == null) {
                            blockEnt = ((EntityBlock)state.getBlock()).newBlockEntity(pos, state);
                            lvl.setBlockEntity(blockEnt);
                        }
                        assert blockEnt != null;
                        //? if >=1.20.5 {
                        blockEnt.loadWithComponents(entity, lvl.registryAccess());
                        //?} else {
                        /*blockEnt.load(entity);
                        *///?}
                        cont.entity = blockEnt;
                    }
                }
            }
        }, false);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.set_block",
            overloads = {
                    @LuaMethodOverload(
                            argumentNames = { "pos", "block" },
                            argumentTypes = { FiguraVec3.class, BlockStateAPI.class }
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "pos", "block" },
                            argumentTypes = { FiguraVec3.class, String.class }
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "blockstate" },
                            argumentTypes = { BlockStateAPI.class }
                    )
            }
    )
    public void setBlock(Object pos, Object block) {
        if (pos instanceof FiguraVec3 posFV3) {
            if (block instanceof String stackString) {
                BlockStateAPI bs = WorldAPI.newBlock(stackString, null, null, null);

                if (bs.hasBlockEntity()) {
                    ClientLevel lvl = minecraft.level;
                    assert lvl != null;
                    HolderLookup<Block> lookup = lvl.holderLookup(Registries.BLOCK);
                    var reader = new StringReader(stackString);
                    try {
                        var result = BlockStateParser.parseForBlock(lookup, reader, true);
                        var nbt = result.nbt();
                        var visitor = new StringTagVisitor();
                        SillyUtil.Devlog("nbt: {}", visitor.visit(nbt));
                        setBlockInternal(posFV3.asBlockPos(), result.blockState(), nbt);

                    } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
                        setBlock(posFV3, bs);
                    }
                } else {
                    setBlock(posFV3, bs);
                }
            } else if (block instanceof BlockStateAPI bs) {
                setBlockInternal(posFV3.asBlockPos(), bs.blockState);
            }
        } else if (pos instanceof BlockStateAPI bs) {
            setBlock(bs.getPos(), bs);
        }
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.get_block_entity",
            overloads = {
                    @LuaMethodOverload(
                            argumentNames = { "pos", "callback" },
                            argumentTypes = { FiguraVec3.class, LuaFunction.class }
                    )
            }
    )
    public void getBlockEntity(@LuaNotNil FiguraVec3 pos, @LuaNotNil LuaFunction callback) {
        if (!local) return;
        assert minecraft.player != null;
        if (minecraft.player.hasPermissions(2)) {
            var conn = minecraft.getConnection();
            if (conn != null) {
                try {
                    conn.getDebugQueryHandler().queryBlockEntityTag(pos.asBlockPos(), (nbt) -> {
                        callback.call((nbt != null) ? NbtToLua.convert(nbt) : LuaValue.NIL);
                    });
                } catch (Exception e) {
                    throw new LuaError(e);
                }
            }
        }
    }

    @LuaWhitelist
    @LuaMethodDoc(value = "silly.get_nano_time")
    public Long getNanoTime() {
        return System.nanoTime();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.disconnect",
            overloads = {
                    @LuaMethodOverload(),
                    @LuaMethodOverload(
                            argumentTypes = {String.class},
                            argumentNames = {"msg"}
                    )
            }
    )
    public void disconnect(String msg) {
        msg = msg != null ? msg : "Disconnected";
        Component comp = Component.literal(msg);
        cheatExecutor(plr -> {
            var conn = minecraft.getConnection();
            if (conn != null) {
                conn.getConnection().disconnect(comp);
            }
        });
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.set_fake_blocks_enabled",
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = { Boolean.class },
                            argumentNames = { "state" }
                    )
            },
            aliases = {"setBlocksEnabled"}
    )
    public SillyAPI setFakeBlocksEnabled(Boolean state) {
        if (!local) return this;
        state = state != null && state;
        state = !state;
        var wasDisabled = this.fakeBlocksDisabled;
        this.fakeBlocksDisabled = state;
        ClientLevel lvl = minecraft.level;
        if (lvl == null) return this;
        if (state != wasDisabled)
            if (state)
                SillyBlockHandler.revert(lvl);
            else
                SillyBlockHandler.apply(lvl);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.get_profiler",
            overloads = {
                    @LuaMethodOverload(
                            argumentNames = { "name" },
                            argumentTypes = { String.class },
                            returnType = SillyProfiler.class
                    ),
                    @LuaMethodOverload(
                            returnType = SillyProfiler.class
                    )
            }
    )
    public SillyProfiler getProfiler(String name) {
        if (name == null) return ((AvatarExtensions)avatar).silly$getProfiler();
        Avatar other = SillyUtil.getAvatar(name);
        if (other == null)
            return null;
        return ((AvatarExtensions)other).silly$getProfiler();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.format_script",
            overloads = {
                    @LuaMethodOverload(
                            argumentNames = { "script", "level" },
                            argumentTypes = { String.class, SillyEnums.FORMAT_LEVEL.class },
                            returnType = String.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = { "script" },
                            argumentTypes = { String.class },
                            returnType = String.class
                    )
            }
    )
    public String formatScript(@LuaNotNil String script, String level) {
        if (level == null) {
            level = switch(Configs.FORMAT_SCRIPT.value) {
                case 0 -> "NONE";
                case 1 -> "LIGHT";
                case 2 -> "HEAVY";
                case 3 -> "AST";
                default -> throw new IllegalStateException("Unexpected value: " + Configs.FORMAT_SCRIPT.value);
            };
        }
        // create instance because that shuts intelliJ's screams up
        // altough....
        //noinspection InstantiationOfUtilityClass
        LuaScriptParserInvokers invoker = (LuaScriptParserInvokers) (new LuaScriptParser());
        return switch(level.toUpperCase()) {
            case "NONE" -> invoker.invokeNoMinifier(script);
            case "LIGHT" -> invoker.invokeRegexMinify("script", script);
            case "HEAVY" -> invoker.invokeAggressiveMinify("script", script);
            case "AST" -> invoker.invokeASTMinify("script", script);
            default -> throw new LuaError("Unknown minification level " + level);
        };
    }

    @LuaWhitelist
    public LuaTable freezeTable(@LuaNotNil LuaTable tbl) {
        return SillyUtil.createReadOnlyLuaTable(tbl);
    }

    public Overridable<Long> dayTime = new Overridable<>();
    public Overridable<Float> rainLevel = new Overridable<>();

    @LuaWhitelist
    public SillyAPI setDayTime(Long time) {
        cheatExecutor(plr -> {
            ((ClientLevel.ClientLevelData)plr.level().getLevelData()).setDayTime(time != null ? time : plr.level().getDayTime());
            dayTime.setValue(time);
        });
        return this;
    }

    @LuaWhitelist
    public LuaTable tokenize(@LuaNotNil String code) {
        try {
            Chunk chunk = new LuaParser(new java.io.StringReader(code)).Chunk();
            chunk.accept(new NameResolver());
            var visitor = new SillyScriptVisitor();
            chunk.accept(visitor);
            var tbl = visitor.toTable();
            return tbl;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @LuaWhitelist
    @Unfinished
    public SillyAPI setRainLevel(Float rainLevel) {
        if (rainLevel != null)
            if (rainLevel.isInfinite() | rainLevel.isNaN())
                rainLevel = 0f;
        Float finalRainLevel = rainLevel;
        cheatExecutor(plr -> {
            ((ClientLevel.ClientLevelData)plr.level().getLevelData()).setRaining(finalRainLevel > 0);
            plr.level().setRainLevel(finalRainLevel);
            this.rainLevel.setValue(finalRainLevel);
        });
        return this;
    }

    @LuaWhitelist
    @LuaTypeDoc(name = "SillyVehicleAPI", value = "silly.vehicle")
    public class SillyVehicleAPI {
        SillyAPI parent;
        public SillyVehicleAPI(SillyAPI parent) {
            this.parent = parent;
        }

        @LuaWhitelist
        @LuaMethodDoc(
                value = "silly.vehicle.set_pos",
                overloads = {
                        @LuaMethodOverload(
                                argumentTypes = {FiguraVec3.class},
                                argumentNames = {"pos"}
                        ),
                        @LuaMethodOverload(
                                argumentTypes = { Float.class, Float.class, Float.class },
                                argumentNames = {"x","y","z"}
                        )
                }
        )
        public void setPos(Object x, Float y, Float z) {
            if (!local) return;
            assert minecraft.player != null;
            Entity vehicle = minecraft.player.getVehicle();
            if (vehicle == null) return;
            Vec3 current = vehicle.position();
            FiguraVec3 pos = LuaUtils.parseVec3("setPos", x, y, z, current.x, current.y, current.z);
            if (isVectorOkay(pos))
                cheatExecutor(plr -> {
                    vehicle.setPos(pos.asVec3());
                });
        }


        @LuaWhitelist
        @LuaMethodDoc(
                value = "silly.vehicle.set_rot",
                overloads = {
                        @LuaMethodOverload(
                                argumentTypes = {FiguraVec2.class},
                                argumentNames = {"rot"}
                        ),
                        @LuaMethodOverload(
                                argumentTypes = { Float.class, Float.class },
                                argumentNames = {"x","y"}
                        )
                }
        )
        public void setRot(@LuaNotNil Object x, Float y) {
            assert minecraft.player != null;
            Entity vehicle = minecraft.player.getVehicle();
            if (vehicle == null) return;
            float cur_x = vehicle.getXRot();
            float cur_y = vehicle.getYRot();
            FiguraVec2 rot = LuaUtils.parseVec2("setRot", x, y, cur_x, cur_y);
            if (!Double.isNaN(rot.x) && !Double.isNaN(rot.y))
                cheatExecutor(plr -> {
                    vehicle.setXRot((float)rot.x);
                    vehicle.setYRot((float)rot.y);
                });
        }

        @LuaWhitelist
        @Alias
        public void setVel(Object x, Float y, Float z) {
            setVelocity(x,y,z);
        }

        @LuaWhitelist
        @LuaMethodDoc(
                value = "silly.vehicle.set_velocity",
                overloads = {
                        @LuaMethodOverload(
                                argumentTypes = {FiguraVec3.class},
                                argumentNames = {"velocity"}
                        ),
                        @LuaMethodOverload(
                                argumentTypes = { Float.class, Float.class, Float.class },
                                argumentNames = {"x","y","z"}
                        )
                },
                aliases = {"setVel"}
        )
        public void setVelocity(Object x, Float y, Float z) {
            if (!local) return;
            assert minecraft.player != null;
            Entity vehicle = minecraft.player.getVehicle();
            if (vehicle == null) return;
            Vec3 current = vehicle.getDeltaMovement();
            FiguraVec3 vel = LuaUtils.parseVec3("setVelocity", x, y, z, current.x, current.y, current.z);
            if (isVectorOkay(vel))
                cheatExecutor(plr -> {
                    vehicle.setDeltaMovement(vel.asVec3());
                });
        }
    }

    @LuaWhitelist
    public FiguraVec3 getDeltaMovement() {
        Entity entity = EntityUtils.getEntityByUUID(avatar.owner);
        if (entity instanceof Player plr) {
            var deltaM = plr.getDeltaMovement();
            return FiguraVec3.of(deltaM.x, deltaM.y, deltaM.z);
        }
        return FiguraVec3.of();
    }

    @LuaFieldDoc("silly.vehicle_field")
    @AutoProperty @ReadOnly public SillyVehicleAPI vehicle = new SillyVehicleAPI(this);


    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.set_subscribe_pings",
            overloads = {
                    @LuaMethodOverload(
                            argumentNames = { "uuid", "state" },
                            argumentTypes = { String.class, Boolean.class },
                            returnType = SillyAPI.class
                    )
            }
    )
    public SillyAPI setSubscribePings(String uuid, Boolean state) {
        if (!local) return this;
        try {
            UUID id = UUID.fromString(uuid);
            if (state)
                customSubscriptions.add(id);
            else
                customSubscriptions.remove(id);
            return this;
        }
        catch (IllegalArgumentException e) {
           throw new LuaError("Argument 2: Expected UUID, got string.");
        }
    }

    @LuaWhitelist
    @Alias
    public SillyAPI setBlocksEnabled(Boolean state) {
        return setFakeBlocksEnabled(state);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.set_body_rot",
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = { Float.class },
                            argumentNames = {"rot"}
                    )
            }
    )
    public SillyAPI setBodyRot(Float rot) {
        Entity entity = EntityUtils.getEntityByUUID(avatar.owner);
        if (entity instanceof Player plr) {
            plr.setYBodyRot(rot);
        }
        return this;
    }

    @LuaWhitelist
    @Unfinished
    public SillyAPI ping(String pingFunc, Object... args) {
        if (!local) return this;
        PingFunction actualPing = avatar.luaRuntime.ping.get(pingFunc);
        if (actualPing == null) throw new LuaError("Ping not found: " + pingFunc);

        LuaValue[] v = new LuaValue[args.length];
        for (int i = 0; i < args.length; i++) {
            v[i] = avatar.luaRuntime.typeManager.javaToLua(args[i]).arg1();
        }
        Varargs actualArgs = LuaValue.varargsOf(v);
        boolean isLocal = AvatarManager.localUploaded;
        AvatarManager.localUploaded = true;
        actualPing.invoke(actualArgs);
        AvatarManager.localUploaded = isLocal;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.get_fake_block_info",
            overloads = {
                    @LuaMethodOverload(
                            argumentNames = {"pos"},
                            argumentTypes = {FiguraVec3.class}
                    ),
                    @LuaMethodOverload(
                            argumentNames = {"x", "y", "z"},
                            argumentTypes = {Double.class, Double.class, Double.class}
                    )
            },
            aliases = {"getBlockInfo"}
    )
    public LuaTable getFakeBlockInfo(Object x, Double y, Double z) {
        BlockPos pos = LuaUtils.parseVec3("getFakeBlockInfo", x, y, z).asBlockPos();
        LuaTable ret = LuaValue.tableOf();
        if (SillyBlockHandler.BLOCKS.containsKey(pos)) {
            // backwards compat
            var cont = SillyBlockHandler.BLOCKS.get(pos);
            if (cont.owner != null)
                ret.set(1, cont.owner.toString());
        }
        return ret;
    }

    @LuaWhitelist
    @Alias
    public LuaTable getBlockInfo(Object x, Double y, Double z) {
        return getFakeBlockInfo(x,y,z);
    }


    @LuaWhitelist
    @LuaMethodDoc(
        value = "silly.set_fly",
        overloads = {
            @LuaMethodOverload(
                    argumentTypes = { Boolean.class, Boolean.class },
                    argumentNames = {"mayFly", "isFly"}
            ),
            @LuaMethodOverload(
                    argumentTypes = { Boolean.class },
                    argumentNames = {"mayFly"}
            ),
            @LuaMethodOverload(
                    argumentNames = {},
                    argumentTypes = {}
            )
        },
        aliases = { "setCanFly" }
    )
    public void setFly(Boolean mayFly, Boolean isFly) {
        cheatExecutor(plr -> {
            this.mayFly.setValue(mayFly);
            if (isFly != null)
                minecraft.player.getAbilities().flying = isFly;
        });
    }

    // alias for backwards compat with goofy
    @LuaWhitelist
    @Alias
    public void setCanFly(Boolean canFly, Boolean isFly) {
        setFly(canFly, isFly);
    }

    public boolean isVectorOkay(FiguraVec3 vec) {
        return vec.notNaN() && Double.isFinite(vec.x) && Double.isFinite(vec.y) && Double.isFinite(vec.z);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.set_pos",
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = {FiguraVec3.class},
                            argumentNames = {"pos"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = { Float.class, Float.class, Float.class },
                            argumentNames = {"x","y","z"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = { Float.class, Float.class, Float.class, Boolean.class },
                            argumentNames = {"x","y","z","sendPacket"}
                    )
            }
    )
    public void setPos(@LuaNotNil Object x, Float y, Float z, Object sendPacket) {
        assert minecraft.player != null;
        Vec3 cur = minecraft.player.position();

        // explicitly check if its a boolean,
        // rather than annotating as a boolean.
        // this is mainly for compatibility with
        // Bitslayn's Lift API.
        final boolean sendPacketForReal = sendPacket instanceof Boolean sp && sp;

        FiguraVec3 pos = LuaUtils.parseVec3("setPos", x, y, z, cur.x, cur.y, cur.z);
        if (isVectorOkay(pos))
            cheatExecutor(plr -> {
                if (sendPacketForReal) {
                    ClientPacketListener conn = minecraft.getConnection();
                    if (conn != null) {
                        //? if >=1.21.4 {
                        conn.send(new ServerboundMovePlayerPacket.Pos(pos.x,pos.y,pos.z,plr.onGround(),plr.horizontalCollision));
                        //?} else {
                        /*conn.send(new ServerboundMovePlayerPacket.Pos(pos.x,pos.y,pos.z,plr.onGround()));
                         *///?}
                    }
                } else
                    plr.setPos(pos.asVec3());
            });
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.set_velocity",
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = {FiguraVec3.class},
                            argumentNames = {"velocity"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = { Float.class, Float.class, Float.class },
                            argumentNames = {"x","y","z"}
                    )
            },
            aliases = {"setVel"}
    )
    public void setVelocity(Object x, Float y, Float z) {
        assert minecraft.player != null;
        Vec3 current = minecraft.player.getDeltaMovement();
        FiguraVec3 vel = LuaUtils.parseVec3("setVelocity", x, y, z, current.x, current.y, current.z);
        if (isVectorOkay(vel))
            cheatExecutor(plr -> plr.setDeltaMovement(vel.asVec3()));
    }

    @LuaWhitelist
    @Alias
    public void setVel(@LuaNotNil Object x, Float y, Float z) {
        setVelocity(x,y,z);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.set_rot",
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = {FiguraVec2.class},
                            argumentNames = {"rot"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = { Float.class, Float.class },
                            argumentNames = {"x","y"}
                    )
            }
    )
    public void setRot(@LuaNotNil Object x, Float y) {
        assert minecraft.player != null;
        float cur_x = minecraft.player.getXRot();
        float cur_y = minecraft.player.getYRot();
        FiguraVec2 rot = LuaUtils.parseVec2("setRot", x, y, cur_x, cur_y);
        if (!Double.isNaN(rot.x) && !Double.isNaN(rot.y))
            cheatExecutor(plr -> {
                plr.setXRot((float)rot.x);
                plr.setYRot((float)rot.y);
            });
    }

    @LuaWhitelist
    @LuaMethodDoc(value = "silly.cheats_enabled")
    public boolean cheatsEnabled() {
        AtomicBoolean enabled = new AtomicBoolean(false);
        cheatExecutor(plr -> enabled.set(true), false);
        return enabled.get();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = { String.class },
                            argumentNames = { "username" },
                            returnType = LuaTable.class
                    )
            },
            value = "silly.get_avatar_nameplate"
    )
    public LuaTable getAvatarNameplate(String username) {
        Avatar other = SillyUtil.getAvatar(username);
        LuaTable table = new LuaTable();
        if (other == null) return table;
        String name = other.entityName;
        if (name.isBlank()) name = other.name;
        if (name.isBlank()) name = other.id;
        table.set("CHAT", ObjectUtils.firstNonNull(other.luaRuntime.nameplate.CHAT.getText(), name));
        table.set("ENTITY", ObjectUtils.firstNonNull(other.luaRuntime.nameplate.ENTITY.getText(), name));
        table.set("LIST", ObjectUtils.firstNonNull(other.luaRuntime.nameplate.LIST.getText(), name));

        return table;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = { String.class },
                            argumentNames = { "username" },
                            returnType = String.class
                    )
            },
            value = "silly.get_avatar_color"
    )
    public String getAvatarColor(String username) {
        Avatar other = SillyUtil.getAvatar(username);
        return other != null ? other.color : null;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = {String.class},
                            argumentNames = {"path"}
                    )
            },
            value = "silly.load_local_avatar"
    )
    public void loadLocalAvatar(@LuaNotNil String path) {
        if (!FiguraMod.isLocal(avatar.owner)) return;

        if (path.isBlank()) throw new LuaError("Empty path detected!");

        Path avatarPath = LocalAvatarFetcher.getLocalAvatarDirectory().resolve(path);
        AvatarManager.loadLocalAvatar(avatarPath);
        AvatarList.selectedEntry = avatarPath;
    }

    @Override
    public String toString() {
        return "SillyAPI" + (cheatsEnabled() ? " (Cheats enabled)" : "");
    }
}
