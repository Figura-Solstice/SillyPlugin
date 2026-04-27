package dev.celestial.silly.lua;

import com.llamalad7.mixinextras.lib.apache.commons.tuple.Pair;
import dev.celestial.silly.SillyUtil;
import dev.celestial.silly.helper.CallerContext;
import dev.celestial.silly.SillyPlugin;
import dev.celestial.silly.helper.CyclicalDeque;
import dev.celestial.silly.mixin.RuntimeAccessor;
import net.minecraft.nbt.ByteArrayTag;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.gui.FiguraToast;
import org.figuramc.figura.lua.FiguraLuaRuntime;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.PathUtils;
import org.luaj.vm2.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

@LuaWhitelist
@LuaTypeDoc(name = "BackportsAPI", value = "silly_backports")
public class BackportsAPI {
    public Avatar owner;
    public FiguraLuaRuntime runtime;
    public BackportsAPI(FiguraLuaRuntime runtime) {
        this.runtime = runtime;
        this.owner = runtime.owner;
    }
    public static ThreadLocal<Deque<Pair<UUID, String>>> callerStack = ThreadLocal.withInitial(ArrayDeque::new);
    public static ThreadLocal<CyclicalDeque<String>> ops = ThreadLocal.withInitial(() -> new CyclicalDeque<>(SillyUtil.DEV_MODE ? 256 : 16));

    public static CallerContext openCallerContext(UUID uuid, String context) {
        return CallerContext.Open(uuid, context);
    }

    public static void pushStack(UUID uuid, String context) {
        if (SillyUtil.DEV_MODE)
            ops.get().push("PUSH " + formatStackPair(Pair.of(uuid, context)));
        callerStack.get().push(Pair.of(uuid, context));
    }

    public static void popStack(UUID expectedUUID, String expectedContext) {
        if (SillyUtil.DEV_MODE)
            ops.get().push("POP " + formatStackPair(Pair.of(expectedUUID, expectedContext)));
        Pair<UUID, String> expected = Pair.of(expectedUUID, expectedContext);
        try {
            Pair<UUID, String> item = callerStack.get().pop();
            UUID uuid = item.getLeft();
            if (uuid != expectedUUID || !Objects.equals(expectedContext, item.getRight())) {
                String msg = "silly_backports:getCaller() stack error (expected " + formatStackPair(expected)
                        + ", got " + formatStackPair(item);
                if (DevAPI.throw_on_bad_call_stack)
                    throw new IllegalStateException(msg);
                FiguraToast.sendToast(FiguraText.of("SillyPlugin error"), FiguraText.of(msg), FiguraToast.ToastType.ERROR);
                SillyPlugin.LOGGER.error(msg);
                for (String op : ops.get()) {
                    SillyPlugin.LOGGER.error(" -> {}", op);
                }
                callerStack.get().clear();
            }
        }
        catch (NoSuchElementException e) {
            String msg = "silly_backports:getCaller() stack error (expected " + formatStackPair(expected)
                    + ", was empty";
            if (DevAPI.throw_on_bad_call_stack)
                throw new IllegalStateException(msg);
            FiguraToast.sendToast(FiguraText.of("SillyPlugin error"), FiguraText.of(msg), FiguraToast.ToastType.ERROR);
            SillyPlugin.LOGGER.error(msg);
        }
    }

    private static String formatStackPair(Pair<UUID, String> pair) {
        return "(" + pair.getLeft().toString() + ", " + pair.getRight() + ")";
    }

    @LuaWhitelist
    @LuaMethodDoc(value = "silly_backports.get_caller")
    public String getCaller() {
        Pair<UUID, String> caller = callerStack.get().peek();
        if (caller != null) {
            UUID uuid = caller.getLeft();
            if (uuid != owner.owner) return uuid.toString();
        }
        return null;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "scriptName"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, String.class},
                            argumentNames = {"scriptName", "scriptContents"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, String.class, String.class},
                            argumentNames = {"scriptName", "scriptContents", "side"}
                    )
            },
            value = "silly_backports.add_script"
    )
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
    @LuaMethodDoc(
            value = "silly_backports.get_scripts"
    )
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
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "scriptName"
            ),
            value = "silly_backports.get_script"
    )
    public String getScript(String scriptPath) {
        if (scriptPath == null) scriptPath = "";
        RuntimeAccessor runtimeAccessor = ((RuntimeAccessor)this.runtime);
        Map<String, String> scripts = runtimeAccessor.getScripts();
        LuaFunction getInfoFunction = runtimeAccessor.getGetInfoFunction();
        Path path = PathUtils.getPath(scriptPath);
        return scripts.get(PathUtils.computeSafeString(PathUtils.isAbsolute(path) ? path : PathUtils.getWorkingDirectory(getInfoFunction).resolve(path)));
    }


    @Override
    public String toString() {
        return "BackportsAPI";
    }
}

