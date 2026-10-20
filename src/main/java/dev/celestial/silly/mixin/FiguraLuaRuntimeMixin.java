package dev.celestial.silly.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.lib.apache.commons.tuple.Pair;
import dev.celestial.silly.SillyPermissions;
import dev.celestial.silly.SillyPlugin;
import dev.celestial.silly.SillyUtil;
import dev.celestial.silly.helper.AutoProfile;
import dev.celestial.silly.lua.BackportsAPI;
import dev.celestial.silly.helper.CallerContext;
import dev.celestial.silly.lua.SillyAPI;
import dev.celestial.silly.lua.SillyProfiler;
import dev.celestial.silly.luaj.LuaUncatchableError;
import dev.celestial.silly.not_a_mixin.AvatarExtensions;
import dev.celestial.silly.not_a_mixin.EventsAccessor;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.FiguraLuaRuntime;
import org.figuramc.figura.lua.api.event.LuaEvent;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.BaseLib;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Mixin(value = FiguraLuaRuntime.class, remap = false)
public abstract class FiguraLuaRuntimeMixin {
    @Unique
    public boolean injectIntoError = true;
    @Shadow
    @Final
    public Avatar owner;

    @Shadow
    public abstract void error(Throwable e);

    @Shadow
    @Final
    private Globals userGlobals;
    @Unique
    private ThreadLocal<Instant> endTime = new ThreadLocal<>();

    @Unique
    private LuaFunction silly$getHookFunction;

    @Inject(method = "setupFiguraSandbox", at = @At("HEAD"))
    public void silly$extractGetHook(CallbackInfo ci) {
        silly$getHookFunction = userGlobals.get("debug").checktable().get("gethook").checkfunction();
    }

    @Unique
    private Function<Pair<Pair<LuaFunction, Pair<LuaNumber, LuaNumber>>, FiguraLuaRuntime>, ZeroArgFunction> silly$executiousInterruptus = (pack) -> new ZeroArgFunction() {
        private Pair<LuaFunction, Pair<LuaNumber, LuaNumber>> hook = pack.getLeft();
        private FiguraLuaRuntime owner = pack.getRight();
        private Integer callsRemaining = hook.getRight().getLeft().toint();
        private Integer callSteps = hook.getRight().getRight().toint();
        @Override
            public LuaValue call() {
                if (endTime.get() != null && endTime.get().isBefore(Instant.now())) {
                    owner.error(new LuaUncatchableError("Execution timed out!"));
                    error("Execution timed out!");
                }
                callsRemaining -= callSteps;
                if (callsRemaining <= 0) hook.getLeft().call();
                return null;
            }
    };

    @Unique
    public void silly$injectExecTimeHook() {
        if (this.owner.permissions.get(SillyPermissions.EXEC_TIME) != Integer.MAX_VALUE) {
            Varargs hook = silly$getHookFunction.call().checkfunction();
            LuaFunction hookFunc = hook.arg(1).checkfunction();
            LuaValue mask = hook.arg(2);
            LuaNumber count;
            if (hook.arg(3).isnumber())
                count = hook.arg(3).checknumber();
            else
                count = LuaValue.valueOf(Integer.MAX_VALUE);
            var sethook = ((RuntimeAccessor)this).getSetHookFunction();
            sethook.call(silly$executiousInterruptus.apply(Pair.of(Pair.of(hookFunc, Pair.of(count, LuaValue.valueOf(Integer.min(count.checkint(), 10000)))), (FiguraLuaRuntime) (Object)this)), mask, LuaValue.valueOf(Integer.min(count.checkint(), 10000)));
        }
    }

    @WrapOperation(method = "run", at = @At(value = "INVOKE", target = "Lorg/figuramc/figura/lua/FiguraLuaRuntime;setInstructionLimit(I)V"))
    public void silly$cancelHookIfScriptExecTime(FiguraLuaRuntime instance, int limit, Operation<Void> original) {
        original.call(instance, limit);
        silly$injectExecTimeHook();
    }

    @WrapMethod(method = "run")
    public Varargs runMixin(Object toRun, Avatar.Instructions limit, Object[] args, Operation<Varargs> original) {
        if (owner.permissions.get(SillyPermissions.SCRIPT_EXEC) > 1 || Objects.equals(toRun, "ENTITY_INIT")) {
            boolean removeEndTime = true;
            if (endTime.get() == null)
                endTime.set(Instant.now().plusMillis(this.owner.permissions.get(SillyPermissions.EXEC_TIME)));
            else
                removeEndTime = false;
            try(CallerContext ctx = BackportsAPI.openCallerContext(owner.owner, null, "avatarRun")) {
                if (Objects.equals(toRun, "RENDER")) {
                    SillyProfiler prf = ((AvatarExtensions)owner).silly$getProfiler();
                        if (prf == null) {
                            try {
                                SillyUtil.nikoTricksTheJavaCompiler();
                                return original.call(toRun, limit, args);
                            } catch (LuaUncatchableError e) {
                                error(e);
                            }
                        }
                        try (AutoProfile profiler = AutoProfile.start(prf.lastRenderEventTimes::push)) {
                            try {
                                SillyUtil.nikoTricksTheJavaCompiler();
                                return original.call(toRun, limit, args);
                            } catch (LuaUncatchableError e) {
                                error(e);
                            }
                        }
                } else {
                    try {
                        SillyUtil.nikoTricksTheJavaCompiler();
                        return original.call(toRun, limit, args);
                    } catch (LuaUncatchableError e) {
                        error(e);
                    }
                }
            } finally {
                if (removeEndTime)
                    endTime.remove();
            }
        } else {
            owner.noPermissions.add(SillyPermissions.SCRIPT_EXEC);
        }
        return null;
    }

    @WrapOperation(method = "init", at = @At(value = "INVOKE", target = "Ljava/util/Map;isEmpty()Z"))
    public boolean initMixin(Map<String, String> instance, Operation<Boolean> original) {
        boolean isEmpty = original.call(instance);
        if (!isEmpty) {
            boolean shouldBeEmptyAnyway = owner.permissions.get(SillyPermissions.SCRIPT_EXEC) == 0;
            if (shouldBeEmptyAnyway) {
                owner.noPermissions.add(SillyPermissions.SCRIPT_EXEC);
                return true;
            }
        }
        return isEmpty;
    }

    @WrapMethod(method = "load")
    public LuaValue load(String name, String src, Operation<LuaValue> original) {
        var ret = original.call(name, src);
        return new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                boolean removeEndTime = true;
                try {
                    silly$injectExecTimeHook();
                    if (endTime.get() == null)
                        endTime.set(Instant.now().plusMillis(owner.permissions.get(SillyPermissions.EXEC_TIME)));
                    else
                        removeEndTime = false;
                    return ret.invoke(args);
                } finally {
                    if (removeEndTime)
                        endTime.remove();
                }
            }
        };
    }

    @WrapMethod(method = "initializeScript")
    public Varargs initScriptEnter(String str, Operation<Varargs> original) {
        boolean removeEndTime = true;
        try(CallerContext ctx = BackportsAPI.openCallerContext(owner.owner, null, "initScript/" + str)) {
            silly$injectExecTimeHook();
            if (endTime.get() == null)
                endTime.set(Instant.now().plusMillis(this.owner.permissions.get(SillyPermissions.EXEC_TIME)));
            else
                removeEndTime = false;
            SillyUtil.nikoTricksTheJavaCompiler();
            return original.call(str);
        } catch (LuaUncatchableError e) {
            error(e);
            return null;
        } finally {
            if (removeEndTime)
                endTime.remove();
        }
    }

    @WrapMethod(method = "parseError")
    private static LuaError silly$parseUncatchables(Throwable e, Operation<LuaError> original) {
        if (e instanceof LuaUncatchableError) return new LuaError(e.getMessage());
        return original.call(e);
    }

    @Inject(method="error", at = @At("HEAD"), cancellable = true)
    public void errorMixin(Throwable e, CallbackInfo ci) {
        if (owner.luaRuntime == null) return;
        if (e instanceof LuaUncatchableError) return;
        LuaEvent ev = ((EventsAccessor)owner.luaRuntime.events).silly$getErrorEvent();
        if (ev == null) return;
        if (ev.__len() > 0) {
            if (injectIntoError) {
                injectIntoError = false;
                Varargs res = owner.luaRuntime.run("ERROR", owner.tick, e.getMessage());
                if (res == null) return;
                if (res.arg(1).isboolean() && res.arg(1).checkboolean()) {
                    injectIntoError = true;
                    ci.cancel();
                    return;
                } else if (res.arg(1).isstring()) {
                    String val = res.arg(1).checkjstring();
                    ev.clear();
                    ci.cancel();
                    error(new LuaError(val));
                    return;
                }
            } else {
                ev.clear();
                ci.cancel();
                error(new LuaError("Error occurred during error event: " + e.getMessage()));
                return;
            }
        }
        SillyAPI silly = ((AvatarExtensions)owner).silly$getSilly();
        if (silly != null) silly.cleanup();
    }
}
