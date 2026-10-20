package dev.celestial.silly.lua;

import com.llamalad7.mixinextras.lib.apache.commons.tuple.Pair;
import dev.celestial.silly.SillyUtil;
import dev.celestial.silly.helper.SillyBlockContainer;
import dev.celestial.silly.helper.SillyBlockHandler;
import dev.celestial.silly.mixin.RuntimeAccessor;
import dev.celestial.silly.not_a_mixin.AvatarExtensions;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.SnbtPrinterTagVisitor;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.FiguraLuaRuntime;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.api.world.BlockStateAPI;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.util.UUID;

// lua functions to aid in debugging SillyPlugin.
// enable by running the build script with -dev
@LuaWhitelist
public class DevAPI {
    public Avatar owner;
    public FiguraLuaRuntime runtime;
    public static boolean throw_on_bad_call_stack = true;
    public DevAPI(FiguraLuaRuntime runtime) {
        this.runtime = runtime;
        this.owner = runtime.owner;
    }

    // ...maybe not super useful, since it'd (theoretically) only run
    // while its your turn on the stack
    @LuaWhitelist
    public LuaValue get_caller_stack() {
        LuaTable ret = new LuaTable();
        int index = 1;
        for (Pair<UUID, String> stackValue : BackportsAPI.callerStack.get()) {
            LuaTable value = new LuaTable();
            value.set(1, stackValue.getLeft().toString());
            value.set(2, stackValue.getRight());
            ret.set(index, value);
            index++;
        }

        return ret;
    }

    private LuaTable compileBlockData(BlockPos blockPos, SillyBlockContainer sillyBlockContainer) {
        LuaTable c = new LuaTable();
        c.set("coordinates", runtime.typeManager.javaToLua(FiguraVec3.of(blockPos.getX(), blockPos.getY(), blockPos.getZ())).arg1());
        BlockStateAPI blockstate = new BlockStateAPI(sillyBlockContainer.state, sillyBlockContainer.coordinates);
        c.set("blockstate", blockstate.toStateString());
        if (sillyBlockContainer.entity != null) {
            //? if neoforge {
            /*var snbt = (new SnbtPrinterTagVisitor()).visit(sillyBlockContainer.entity.getPersistentData());
            *///?} else {
            //? if >=1.20.5 {
            var tag = sillyBlockContainer.entity.saveWithFullMetadata(Minecraft.getInstance().level.registryAccess());
            //?} else {
            /*var tag = sillyBlockContainer.entity.saveWithFullMetadata();
            *///?}
            var snbt = (new SnbtPrinterTagVisitor()).visit(tag);
            //?}
            c.set("entity", snbt);
        }
        if (sillyBlockContainer.owner != null)
            c.set("owner", sillyBlockContainer.owner.toString());
        return c;
    }

    @LuaWhitelist
    public LuaTable getBlockHandlerState() {
        LuaTable ret = new LuaTable();
        LuaTable blocks = new LuaTable();
        SillyBlockHandler.BLOCKS.forEach((blockPos, sillyBlockContainer) -> {
            blocks.set(String.format("{ %d, %d, %d }", blockPos.getX(), blockPos.getY(), blockPos.getZ()), compileBlockData(blockPos,sillyBlockContainer));
        });
        LuaTable real_blocks = new LuaTable();
        SillyBlockHandler.REAL_BLOCKS.forEach((blockPos, sillyBlockContainer) -> {
            real_blocks.set(String.format("{ %d, %d, %d }", blockPos.getX(), blockPos.getY(), blockPos.getZ()), compileBlockData(blockPos,sillyBlockContainer));
        });
        ret.set("blocks", blocks);
        ret.set("real_blocks", real_blocks);

        return ret;
    }

    @LuaWhitelist
    public LuaTable getAliveObjects(String name) {
        Avatar avatar = SillyUtil.getAvatar(name);
        if (avatar == null || avatar.luaRuntime == null) return new LuaTable();
        var tracker = ((AvatarExtensions)avatar).silly$getAllocationTracker();
        LuaTable tbl = new LuaTable();
        tracker.findCulprits().forEach(tbl::set);
        return SillyUtil.createReadOnlyLuaTable(tbl);
    }

    @LuaWhitelist
    public LuaTable get_stack_ops() {
        LuaTable ret = new LuaTable();
        int i = 1;
        for (String op : BackportsAPI.ops.get()) {
            ret.set(i, op);
            i++;
        }
        return ret;
    }

    @LuaWhitelist
    public void disable_throw_on_bad_call_stack() {
        throw_on_bad_call_stack = false;
    }

    @LuaWhitelist
    public void error(String name) {
        SillyUtil.getAvatar(name).luaRuntime.error(new LuaError("MEOOOOOOOW :3"));
    }

    @LuaWhitelist
    public void printAs(String name, String val) {
        Avatar av = SillyUtil.getAvatar(name);
        Globals g = ((RuntimeAccessor)av.luaRuntime).getGlobals();
        g.get("print").call(val);
    }
}
