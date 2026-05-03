package dev.celestial.silly.mixin;

import dev.celestial.silly.SillyPlugin;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.lua.FiguraLuaPrinter;
import org.figuramc.figura.lua.FiguraLuaRuntime;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FiguraLuaPrinter.class, remap = false)
public class FiguraLuaPrinterMixin {
    @Unique
    private static void silly$doTheFunny(FiguraLuaRuntime runtime, CallbackInfoReturnable<LuaValue> cir) {
        LuaValue orig = cir.getReturnValue();
        LuaFunction func = orig.checkfunction();
        cir.setReturnValue(new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (runtime.owner.permissions.get(SillyPlugin.PRINT) == 1)
                    return func.invoke(args);
                return LuaValue.NIL;
            }
        });
    }
    @Inject(method = "lambda$static$0", at = @At("RETURN"), cancellable = true)
    private static void silly$printPermission1(FiguraLuaRuntime runtime, CallbackInfoReturnable<LuaValue> cir) {
        silly$doTheFunny(runtime, cir);
    }
    @Inject(method = "lambda$static$1", at = @At("RETURN"), cancellable = true)
    private static void silly$printPermission2(FiguraLuaRuntime runtime, CallbackInfoReturnable<LuaValue> cir) {
        silly$doTheFunny(runtime, cir);
    }
    @Inject(method = "lambda$static$2", at = @At("RETURN"), cancellable = true)
    private static void silly$printPermission3(FiguraLuaRuntime runtime, CallbackInfoReturnable<LuaValue> cir) {
        silly$doTheFunny(runtime, cir);
    }
}
