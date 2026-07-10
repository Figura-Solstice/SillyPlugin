package dev.celestial.silly.mixin.docs;

import dev.celestial.silly.SillyEnums;
import dev.celestial.silly.SillyUtil;
import org.figuramc.figura.lua.docs.FiguraListDocs;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

@Mixin(targets = {"org.figuramc.figura.lua.docs.FiguraListDocs$ListDoc"}, remap = false)
@Unique
public class FiguraListDocsMixin {
    @Unique private static sun.misc.Unsafe silly$unsafe;
    static {
        // Source - https://stackoverflow.com/a/54030092
        // Posted by user3408531, modified by community. See post 'Timeline' for change history
        // Retrieved 2026-07-04, License - CC BY-SA 4.0

        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            silly$unsafe = (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    // doesnt work; mixin says it cant find $VALUES
//    @Shadow
//    @Final
//    @Mutable
//    private static Object $VALUES;

    @Unique
    private static List<Object> silly$fakeValues = new ArrayList<>();
    @Unique
    private static HashMap<String, Object> silly$quickFakeValuesLookup = new HashMap<>();
    @Unique
    private static Class<?> silly$class = null;

    // kill me please this is hell
    @Unique
    private static void silly$addToDocs(String $NAME, Supplier<Object> supplier, String name, String id, int split) throws Throwable {
        if (silly$class == null) {
            for (Class<?> maybe : FiguraListDocs.class.getDeclaredClasses()) {
                if (maybe.getSimpleName().equals("ListDoc")) {
                    silly$class = maybe;
                }
            }
        }
        if (silly$class == null) throw new AssertionError("Could not find ListDoc enum class!");

        Field f = silly$class.getDeclaredField("$VALUES");
        SillyUtil.Devlog("Get got");
        f.setAccessible(true);
        SillyUtil.Devlog("Accessible");

        Object base = silly$unsafe.staticFieldBase(f);
        SillyUtil.Devlog("Base {}", base);
        long offset = silly$unsafe.staticFieldOffset(f);
        SillyUtil.Devlog("Offset {}", offset);

        f.setAccessible(true);
        // errors HERE lmao VVVV
        // Caused by: java.lang.IllegalArgumentException: Can not get static final [Lorg.figuramc.figura.lua.docs.FiguraListDocs$ListDoc; field org.figuramc.figura.lua.docs.FiguraListDocs$ListDoc.$VALUES
        // curiously, only happens on the second pass
        Object values = f.get(null); // Object -> ListDoc[]
        SillyUtil.Devlog("Values");

        Object[] values2 = (Object[])values; // Object[] -> ListDoc[]
        ArrayList<Object> list = new ArrayList<>(Arrays.stream(values2).toList());
        var last = list.get(list.size() - 1);
        var castedLast = silly$class.cast(last);
        var ordinal = (int)silly$class.getMethod("ordinal").invoke(castedLast);
        int nextOrdinal = ordinal + silly$fakeValues.size() + 1;
        var constr = MethodHandles.lookup().unreflectConstructor(
                silly$class.getDeclaredConstructor(String.class, int.class,
                        Supplier.class, String.class, String.class, int.class));
        var instance = constr.invoke($NAME, nextOrdinal, supplier, name, id, split);
        list.add(instance);

        var arr = Array.newInstance(silly$class, list.size());
        for (int i = 0; i < list.size(); i++) {
            Array.set(arr, i, list.get(i));
        }

        silly$unsafe.putObject(base, offset, arr);
//        silly$fakeValues.add(instance);
//        silly$quickFakeValuesLookup.put($NAME.toLowerCase(), instance);
    }

//    @Inject(method = "values", at = @At("RETURN"), cancellable = true)
//    private static void silly$ihatethis(CallbackInfoReturnable<Object[]> cir) {
//        ArrayList<Object> list = new ArrayList<>(Arrays.asList(cir.getReturnValue()));
//        for (var value : silly$fakeValues) {
//            list.add(silly$class.cast(value));
//        }
//        Object[] ret = (Object[])Array.newInstance(silly$class, list.size());
//        System.arraycopy(list.toArray(), 0, ret, 0, list.size());
//        cir.setReturnValue(ret);
//    }
//
//    @Inject(method = "valueOf", at = @At("HEAD"), cancellable = true)
//    private static void silly$ihatethis2(String name, CallbackInfoReturnable<Object> cir) {
//        if (silly$quickFakeValuesLookup.containsKey(name.toLowerCase(Locale.ROOT)))
//            cir.setReturnValue(silly$quickFakeValuesLookup.get(name.toLowerCase(Locale.ROOT)));
//    }

    static {
        try {
            silly$addToDocs("SILLY_GUI_ELEMENT", () -> {
                var ret = new LinkedHashSet<String>();
                for (var value : SillyEnums.GUI_ELEMENT.values())
                    ret.add(value.name());
                return ret;
            }, "SillyGUIElement", "silly_gui_element", 1);
            silly$addToDocs("SILLY_FORMAT_LEVEL", () -> {
                var ret = new LinkedHashSet<String>();
                for (var value : SillyEnums.FORMAT_LEVEL.values())
                    ret.add(value.name());
                return ret;
            }, "SillyFormatLevel", "silly_format_level", 1);
            silly$addToDocs("SILLY_VR_BODY_PART", () -> {
                var ret = new LinkedHashSet<String>();
                for (var value : SillyEnums.VR_BODY_PART.values())
                    ret.add(value.name());
                return ret;
            }, "SillyVRBodyPart", "silly_vr_body_part", 1);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
