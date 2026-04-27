package dev.celestial.silly.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.lib.apache.commons.tuple.Pair;
import dev.celestial.silly.annotations.AutoProperty;
import dev.celestial.silly.annotations.AutoPropertyWhitelist;
import dev.celestial.silly.annotations.ReadOnly;
import dev.celestial.silly.lua.BackportsAPI;
import dev.celestial.silly.not_a_mixin.AvatarAccessor;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.lua.FiguraLuaPrinter;
import org.figuramc.figura.lua.LuaTypeManager;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.UUID;

@Mixin(value = LuaTypeManager.class, remap = false)
public abstract class LuaTypeManagerMixin {
    @Shadow
    public abstract Varargs javaToLua(Object par1);

    @Shadow
    public abstract Object luaToJava(LuaValue par1);

    @Shadow
    public abstract Object luaVarargToJava(Varargs par1, int par2, Class<?> par3);

    @WrapOperation(method = "generateMetatableFor", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    public <K, V> V silly$injectAutoProperties(Map<K,V> instance, K k, V v, Operation<V> original) {
        Class<?> clazz = (Class<?>) k;
        LuaTable metatable = (LuaTable) v;
        if (!clazz.isAnnotationPresent(AutoPropertyWhitelist.class)) {
            return original.call(instance, k,v);
        }

        var __indexV = metatable.get("__index");
        if (!__indexV.istable())
            return original.call(instance, k,v);

        LuaTable __index = __indexV.checktable();
        LuaTable getters = new LuaTable();
        LuaTable setters = new LuaTable();
        for (var field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(AutoProperty.class)) continue;
            getters.set(field.getName(), new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue t) {
                    try {
                        return javaToLua(field.get(t.checkuserdata(clazz))).arg1();
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("AutoProperty " + field.getName() + " on class " + clazz.getSimpleName() + " was inaccessible to the type manager!\n(this is an issue with SillyPlugin, not your code!)");
                    }
                }
            });
            if (!field.isAnnotationPresent(ReadOnly.class))
                setters.set(field.getName(), new TwoArgFunction() {
                    @Override
                    public LuaValue call(LuaValue arg1, LuaValue arg2) {
                        var obj = arg1.checkuserdata(clazz);
                        try {
                            var type = field.getType();
                            var val = switch (type.getName()) {
                                case "java.lang.Number", "java.lang.Double", "double" -> arg2.checkdouble();
                                case "java.lang.String" -> arg2.checkjstring();
                                case "java.lang.Boolean", "boolean" -> arg2.toboolean();
                                case "java.lang.Float", "float" -> (float) arg2.checkdouble();
                                case "java.lang.Integer", "int" -> arg2.checkint();
                                case "java.lang.Long", "long" -> arg2.checklong();
                                case "org.luaj.vm2.LuaTable" -> arg2.checktable();
                                case "org.luaj.vm2.LuaFunction" -> arg2.checkfunction();
                                case "org.luaj.vm2.LuaValue" -> arg2.arg1();
                                case "java.lang.Object" -> luaToJava(arg2.arg1());
                                default -> type.getName().startsWith("[") ? luaVarargToJava(arg2, 1, type) : arg2.checkuserdata(1, type);
                            };
                            field.set(obj, val);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException("AutoProperty on class " + clazz.getSimpleName() + " was unwritable to the type manager!\n(this is an issue with SillyPlugin, not your code!)");
                        }
                        return null;
                    }
                });
        }
        LuaValue nestedMeta = __index.getmetatable();
        if (nestedMeta == null || !getters.istable())
            nestedMeta = new LuaTable();
        LuaTable nestedMetatable = nestedMeta.checktable();
        nestedMetatable.set("__index", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue arg1, LuaValue arg2) {
                LuaValue func = getters.get(arg2);
                if (func == LuaValue.NIL) return LuaValue.NIL;
                Pair<UUID, String> caller = BackportsAPI.callerStack.get().peek();
                if (caller == null) return LuaValue.NIL;
                var owner = caller.getLeft();
                var avatar = AvatarManager.getAvatarForPlayer(owner);
                if (avatar == null) return LuaValue.NIL;
                var instance = ((AvatarAccessor)avatar).silly$getUserData(clazz);

                return func.checkfunction().call(javaToLua(instance).arg1());
            }
        });
        metatable.set("__newindex", new ThreeArgFunction() {
            @Override
            public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {
                var set = setters.get(arg2);
                if (set == LuaValue.NIL) {
                    error("Attempt to set read-only value " + arg2 + "!");
                }
                Pair<UUID, String> caller = BackportsAPI.callerStack.get().peek();
                if (caller == null) return LuaValue.NIL;
                var owner = caller.getLeft();
                var avatar = AvatarManager.getAvatarForPlayer(owner);
                if (avatar == null) return LuaValue.NIL;
                var instance = ((AvatarAccessor)avatar).silly$getUserData(clazz);
                var userdata = javaToLua(instance).arg1();

                set.checkfunction().call(userdata, arg3);
                return arg3;
            }
        });
        __index.setmetatable(nestedMetatable);

        return original.call(instance, clazz, metatable);
    }
}
