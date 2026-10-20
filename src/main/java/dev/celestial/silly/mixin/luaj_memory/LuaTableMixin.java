package dev.celestial.silly.mixin.luaj_memory;

import com.llamalad7.mixinextras.sugar.Local;
import dev.celestial.silly.SillyUtil;
import dev.celestial.silly.luaj.LuaOOM;
import dev.celestial.silly.luaj.memory.AllocationTracker;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

@Mixin(value = LuaTable.class, remap = false)
public abstract class LuaTableMixin {
    @Shadow
    public abstract int rawlen();

    @Shadow
    protected LuaValue[] array;

    @Unique
    private static final Field silly$hashField;
    @Unique
    private static final Object[] silly$no_buckets;

    @Unique
    private static final Class<?> silly$strongSlotClass;

    // i hate access modifiers :sob:
    @Unique
    private static void silly$maybeTrackSlots(Object[] misery) throws InvocationTargetException, IllegalAccessException, LuaOOM {
        var alloc = SillyUtil.getExecutingAllocationTracker();
        if (alloc.isPresent())
            for (var slot : misery) {
                if (silly$strongSlotClass.isInstance(slot)) {
                    alloc.get().track(slot, AllocationTracker.OBJECT_SIZE + AllocationTracker.REFERENCE_SIZE * 2);
                }
            }
    }

    static {
        try {
            silly$hashField = LuaTable.class.getDeclaredField("hash");
            silly$hashField.setAccessible(true);
            var nbucket = LuaTable.class.getDeclaredField("NOBUCKETS");
            nbucket.setAccessible(true);
            silly$no_buckets = (Object[]) nbucket.get(null);
            silly$strongSlotClass = Class.forName("org.luaj.vm2.LuaTable$StrongSlot");
        } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Inject(method = "presize(II)V", at = @At("RETURN"))
    public void silly$trackPresize(int narray, int nhash, CallbackInfo ci) throws LuaOOM, IllegalAccessException, InvocationTargetException {
        var alloc = SillyUtil.getExecutingAllocationTracker();
        if (alloc.isEmpty()) return;
        if (this.array != LuaValue.NOVALS) {
            alloc.get().track(this.array, AllocationTracker.OBJECT_SIZE + this.array.length * AllocationTracker.REFERENCE_SIZE);
        }
        if (silly$hashField.get(this) != silly$no_buckets) {
            silly$maybeTrackSlots((Object[])silly$hashField.get(this));
        }
    }

    @Inject(method = "resize", at = @At("RETURN"))
    private static void silly$trackResize(LuaValue[] old, int n, CallbackInfoReturnable<LuaValue[]> cir) throws LuaOOM {
        var alloc = SillyUtil.getExecutingAllocationTracker();
        if (alloc.isPresent())
            alloc.get().track(cir.getReturnValue(), AllocationTracker.OBJECT_SIZE + n * AllocationTracker.REFERENCE_SIZE);
    }

    @Inject(method = "rehash", at = @At(value = "INVOKE", target = "Ljava/lang/System;arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V"))
    public void silly$trackRehash(int newKey, CallbackInfo ci, @Local(name = "newArray") LuaValue[] newArray) throws LuaOOM {    var alloc = SillyUtil.getExecutingAllocationTracker();
        if (alloc.isPresent())
            alloc.get().track(newArray, AllocationTracker.OBJECT_SIZE + newArray.length * AllocationTracker.REFERENCE_SIZE);
    }

//    @Inject(method = "rehash", at = @At("TAIL"))
//    public void silly$trackRehashedHash(int newKey, CallbackInfo ci) throws IllegalAccessException, LuaOOM, InvocationTargetException {
//        if (silly$hashField.get(this) != silly$no_buckets) {
//            silly$maybeTrackSlots((Object[])silly$hashField.get(this));
//        }
//    }
    @Inject(method = "keys", at = @At("RETURN"))
    public void silly$trackKeys(CallbackInfoReturnable<LuaValue[]> cir) throws LuaOOM {
        var alloc = SillyUtil.getExecutingAllocationTracker();
        if (alloc.isPresent())
            alloc.get().track(cir.getReturnValue(), AllocationTracker.OBJECT_SIZE + cir.getReturnValue().length * AllocationTracker.REFERENCE_SIZE);
    }
}
