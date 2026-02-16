package dev.celestial.silly.lua;

import dev.celestial.silly.SillyEnums;
import dev.celestial.silly.SillyPlugin;
import dev.celestial.silly.SillyUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.ObjectUtils;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.avatar.local.LocalAvatarFetcher;
import org.figuramc.figura.avatar.local.LocalAvatarLoader;
import org.figuramc.figura.backend2.NetworkStuff;
import org.figuramc.figura.gui.widgets.lists.AvatarList;
import org.figuramc.figura.lua.FiguraLuaPrinter;
import org.figuramc.figura.lua.FiguraLuaRuntime;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.api.world.BlockStateAPI;
import org.figuramc.figura.lua.api.world.WorldAPI;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.permissions.PermissionManager;
import org.figuramc.figura.utils.LuaUtils;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@LuaWhitelist
@LuaTypeDoc(name = "SillyAPI", value = "silly")
public class SillyAPI {
    public final Avatar avatar;
    public final FiguraLuaRuntime runtime;
    public final Minecraft minecraft;
    public boolean mayFlyOverride = false;
    public boolean mayFly = false;
    public boolean noclip = false;
    public boolean local;
    public Set<SillyEnums.GUI_ELEMENT> disabledElements = new HashSet<>();

    public SillyAPI(Avatar avatar) {
        SillyPlugin.FakeBlocks.put(avatar.owner, new Hashtable<>());
        this.avatar = avatar;
        this.runtime = avatar.luaRuntime;
        this.minecraft = Minecraft.getInstance();
        local = avatar.isHost;
        if (local) SillyPlugin.hostInstance = this;
    }

    public SillyAPI(FiguraLuaRuntime runtime) {
        this(runtime.owner);
    }

    public void cleanup() {
        SillyPlugin.FakeBlocks.remove(avatar.owner);

        if (!local) return; // START host cleanup
        cheatExecutor(plr -> SillyUtil.antiGhost());
        if (minecraft.player != null) {
            Abilities a = minecraft.player.getAbilities();
            if (a.flying && !a.mayfly && this.mayFly && this.mayFlyOverride) {
                a.flying = false;
            }
        }
        SillyPlugin.hostInstance = null;
    }

    public void cheatExecutor(Consumer<LocalPlayer> callback) {
        cheatExecutor(callback, true);
    }

    public void cheatExecutor(Consumer<LocalPlayer> callback, boolean mustBeHost) {
        if (mustBeHost && !local) return;
        if (!(minecraft.player instanceof LocalPlayer)) return;
        if (minecraft.gameMode == null) return;

        ClientPacketListener con = minecraft.getConnection();
        if (con == null) return;
        ServerData servDt = con.getServerData();
        Component motd = servDt != null ? servDt.motd : Component.empty();

        if (!(minecraft.player.hasPermissions(2)
                || minecraft.gameMode.getPlayerMode().isCreative()
                || minecraft.isSingleplayer()
                || motd.getString().contains("§s§i§l§l§y§p§l§u§g§i§n"))) return;
        callback.accept(minecraft.player);
    }

    @LuaWhitelist
    @LuaMethodDoc("silly.cat")
    public void cat() {
        if (!local) return;
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
        int value = avatar.permissions.get(SillyPlugin.BUMPSCOCITY);
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
                            argumentNames = { "element", "state" }
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {LuaTable.class, Boolean.class},
                            argumentNames = { "elements", "state" }
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {SillyEnums.GUI_ELEMENT.class},
                            argumentNames = { "element" }
                    )
            },
            aliases = { "setRenderHudElement" }
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
    public SillyAPI setRenderHudElement(@LuaNotNil String element, Boolean state) {
        return setHudElementVisible(element, state);
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

    private void setBlockInternal(BlockPos pos, BlockState state) {
        cheatExecutor(plr -> {
            if (avatar.permissions.get(SillyPlugin.FAKE_BLOCKS) != 1) {
                avatar.noPermissions.add(SillyPlugin.FAKE_BLOCKS);
                return;
            } else {
                avatar.noPermissions.remove(SillyPlugin.FAKE_BLOCKS);
            }
            if (minecraft.level != null && minecraft.level.isClientSide) {
                ClientLevel lvl = minecraft.level;
                SillyPlugin.FakeBlocks.get(avatar.owner).put(pos, state);
                lvl.setBlock(pos, state, 2);
            }
        }, false);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "set_block",
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
        if (pos instanceof BlockStateAPI state) {
            BlockPos bpos = state.getPos().asBlockPos();
            setBlockInternal(bpos, state.blockState);
        } else if (pos instanceof FiguraVec3 posFV3) {
            if (block instanceof BlockStateAPI state) {
                setBlockInternal(posFV3.asBlockPos(), state.blockState);
            } else if (block instanceof String stackString) {
                BlockStateAPI bs = WorldAPI.newBlock(stackString, null, null, null);
                setBlock(posFV3, bs);
            } else if (block == null) {
                SillyPlugin.FakeBlocks.get(avatar.owner).remove(pos);
            }
        }
    }


    @LuaWhitelist
    @LuaMethodDoc(
        value = "silly.set_fly",
        overloads = {
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
    public void setFly(Boolean mayFly) {
        cheatExecutor(plr -> {
            if (mayFly == null) {
                this.mayFlyOverride = false;
            } else {
                this.mayFlyOverride = true;
                this.mayFly = mayFly;
            }
        });
    }

    // alias for backwards compat with goofy
    @LuaWhitelist
    public void setCanFly(Boolean canFly) {
        setFly(canFly);
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
                    )
            }
    )
    public void setPos(@LuaNotNil Object x, Float y, Float z) {
        FiguraVec3 pos = LuaUtils.parseVec3("setPos", x, y, z);
        if (isVectorOkay(pos))
            cheatExecutor(plr -> plr.setPos(pos.asVec3()));
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
            }
    )
    public void setVelocity(@LuaNotNil Object x, Float y, Float z) {
        FiguraVec3 vel = LuaUtils.parseVec3("setVelocity", x, y, z);
        if (isVectorOkay(vel))
            cheatExecutor(plr -> plr.setDeltaMovement(vel.asVec3()));
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
        FiguraVec2 rot = LuaUtils.parseVec2("setRot", x, y);
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
        cheatExecutor(plr -> enabled.set(true));
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
        table.set("CHAT", ObjectUtils.firstNonNull(avatar.luaRuntime.nameplate.CHAT.getText(), name));
        table.set("ENTITY", ObjectUtils.firstNonNull(avatar.luaRuntime.nameplate.ENTITY.getText(), name));
        table.set("LIST", ObjectUtils.firstNonNull(avatar.luaRuntime.nameplate.LIST.getText(), name));

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

    // supposedly this function was problematic as a command.
    /*
    @LuaWhitelist
    @LuaMethodDoc(value = "silly.upload_avatar")
    public void uploadAvatar() {
        if (!FiguraMod.isLocal(avatar.owner)) return;
        try {
            // figura i hate your code :skull:
            LocalAvatarLoader.loadAvatar(null, null);
        } catch (Exception ignored) {}
        NetworkStuff.uploadAvatar(avatar);
        AvatarList.selectedEntry = null;
    }
    */

    @Override
    public String toString() {
        return "SillyAPI" + (cheatsEnabled() ? " (Cheats enabled)" : "");
    }
}
