package dev.celestial.silly.lua;

import com.llamalad7.mixinextras.lib.apache.commons.tuple.Pair;
import dev.celestial.silly.SillyPlugin;
import dev.celestial.silly.mixin.RuntimeAccessor;
import net.minecraft.nbt.ByteArrayTag;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.FiguraLuaRuntime;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.utils.PathUtils;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.VarArgFunction;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

@LuaWhitelist
public class BackportsAPI {
    public Avatar owner;
    public FiguraLuaRuntime runtime;
    public BackportsAPI(FiguraLuaRuntime runtime) {
        this.runtime = runtime;
        this.owner = runtime.owner;
    }
    public static Deque<Pair<UUID, String>> callerStack = new ArrayDeque<>();

    public static CallerContext openCallerContext(UUID uuid, String context) {
        return CallerContext.Open(uuid, context);
    }

    public static void pushStack(UUID uuid, String context) {
//        SillyPlugin.LOGGER.info("PUSH {}", formatStackPair(Pair.of(uuid, context)));
        callerStack.push(Pair.of(uuid, context));
    }

    public static void popStack(UUID expectedUUID, String expectedContext) {
//        SillyPlugin.LOGGER.info("POP {}", formatStackPair(Pair.of(expectedUUID, expectedContext)));
        Pair<UUID, String> item = callerStack.pop();
        UUID uuid = item.getLeft();
        Pair<UUID, String> expected = Pair.of(expectedUUID, expectedContext);
        if (uuid != expectedUUID || !Objects.equals(expectedContext, item.getRight()))
            throw new IllegalStateException("Caller stack exploded (expected " + formatStackPair(expected)
                    + ", got " + formatStackPair(item));
    }

    private static String formatStackPair(Pair<UUID, String> pair) {
        return "(" + pair.getLeft().toString() + ", " + pair.getRight() + ")";
    }

    @LuaWhitelist
    public String getCaller() {
        Pair<UUID, String> caller = callerStack.peek();
        if (caller != null) return caller.getLeft().toString();
        return null;
    }

    @LuaWhitelist
    public void addScript(@LuaNotNil String path, String contents, String side) {
        if (side == null) side = "BOTH";
        side = side.toUpperCase();
        boolean nbt = !side.equals("RUNTIME");
        boolean runtime = !side.equals("NBT");
        RuntimeAccessor runtimeAccessor = ((RuntimeAccessor)this.runtime);
        Map<String, String> scripts = runtimeAccessor.getScripts();
        LuaFunction getInfoFunction = runtimeAccessor.getGetInfoFunction();

        Path filepath = PathUtils.getPath(path);
        Path dir = PathUtils.getWorkingDirectory(getInfoFunction);
        String scriptName = PathUtils.computeSafeString(PathUtils.getPath(PathUtils.computeSafeString(
                PathUtils.isAbsolute(path) ? filepath : dir.resolve(filepath)
        )));
        String scriptNameNbt = scriptName.replace('/','.');

        Map<String, Varargs> loadedScripts = runtimeAccessor.getLoadedScripts();
        if (runtime) loadedScripts.remove(scriptName);
        if (contents == null) {
            if (nbt) owner.nbt.getCompound("scripts").remove(scriptNameNbt);
            if (runtime) scripts.remove(scriptName);
            return;
        }
        if (runtime) scripts.put(scriptName, contents);
        if (nbt) owner.nbt.getCompound("scripts").put(scriptNameNbt,new ByteArrayTag(contents.getBytes(StandardCharsets.UTF_8)));
    }

    @LuaWhitelist
    public LuaTable getScripts(String path) {
        if (path == null) path = "";
        // iterate over all script names and add them if their name starts with the path query
        Map<String, String> scripts = ((RuntimeAccessor)runtime).getScripts();
        LuaTable table = new LuaTable();
        if(path.isEmpty()){
            for (String s : scripts.keySet()) {
                table.set(s,scripts.get(s));
            }
        }else{
            for (String s : scripts.keySet()) {
                if(!s.startsWith(path)) continue;
                table.set(s,scripts.get(s));
            }
        }

        return table;
    }

    @LuaWhitelist
    public String getScript(String scriptPath) {
        if (scriptPath == null) scriptPath = "";
        RuntimeAccessor runtimeAccessor = ((RuntimeAccessor)this.runtime);
        Map<String, String> scripts = runtimeAccessor.getScripts();
        LuaFunction getInfoFunction = runtimeAccessor.getGetInfoFunction();
        Path path = PathUtils.getPath(scriptPath);
        return scripts.get(PathUtils.computeSafeString(PathUtils.isAbsolute(path) ? path : PathUtils.getWorkingDirectory(getInfoFunction).resolve(path)));
    }
}

