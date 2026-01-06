package dev.celestial.silly.lua;

import dev.celestial.silly.SillyPlugin;
import dev.celestial.silly.SillyUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Abilities;
import org.apache.commons.lang3.ObjectUtils;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.avatar.local.LocalAvatarFetcher;
import org.figuramc.figura.avatar.local.LocalAvatarLoader;
import org.figuramc.figura.backend2.NetworkStuff;
import org.figuramc.figura.gui.widgets.lists.AvatarList;
import org.figuramc.figura.lua.FiguraLuaRuntime;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.api.world.BlockStateAPI;
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
import java.util.Hashtable;
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

    public SillyAPI(Avatar avatar) {
        SillyPlugin.FakeBlocks.put(avatar.id, new Hashtable<>());
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
        SillyPlugin.FakeBlocks.remove(avatar.id);

        if (!local) return; // START host cleanup
        if (minecraft.player != null) {
            Abilities a = minecraft.player.getAbilities();
            if (a.flying && !a.mayfly && this.mayFly && this.mayFlyOverride) {
                a.flying = false;
            }
        }
        SillyPlugin.hostInstance = null;
    }

    public void cheatExecutor(Consumer<LocalPlayer> callback) {
        if (!local) return;
        if (!(minecraft.player instanceof LocalPlayer)) return;
        if (minecraft.gameMode == null) return;
//        if (!(minecraft.player.hasPermissions(2) || minecraft.gameMode.getPlayerMode().isCreative() || minecraft.isSingleplayer() || minecraft.player.getTags().contains("silly_cheats_allowed"))) return;
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
    public void setBlock(BlockStateAPI state) {
        cheatExecutor(plr -> {
            if (avatar.permissions.get(SillyPlugin.FAKE_BLOCKS) != 1) {
                avatar.noPermissions.add(SillyPlugin.FAKE_BLOCKS);
                return;
            } else {
                avatar.noPermissions.remove(SillyPlugin.FAKE_BLOCKS);
            }
            if (minecraft.level != null) {
                ClientLevel lvl = minecraft.level;
                FiguraVec3 posFV3 = state.getPos().floor();
                BlockPos pos = new BlockPos((int)posFV3.x, (int)posFV3.y, (int)posFV3.z);
                SillyPlugin.FakeBlocks.get(avatar.id).put(pos, state.blockState);
                lvl.setBlock(pos, state.blockState, 2);
            }
        });

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
        }
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
        if (pos.notNaN())
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
        if (vel.notNaN())
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

    @Override
    public String toString() {
        return "SillyAPI" + (cheatsEnabled() ? " (Cheats enabled)" : "");
    }
}
