package dev.celestial.silly.mixin;

import dev.celestial.silly.not_a_mixin.EventsAccessor;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.api.event.EventsAPI;
import org.figuramc.figura.lua.api.event.LuaEvent;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = EventsAPI.class, remap = false)
public class EventsAPIMixin implements EventsAccessor {
    @Unique
    @LuaWhitelist
    @LuaFieldDoc("events.error")
    public LuaEvent ERROR;

    @Unique
    @LuaWhitelist
    @LuaFieldDoc("events.gui_render")
    public LuaEvent GUI_RENDER;

    @Shadow
    @Final
    private Map<String, LuaEvent> events;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initMixin(CallbackInfo ci) {
        ERROR = events.put("ERROR", new LuaEvent());
        GUI_RENDER = events.put("GUI_RENDER", new LuaEvent());
    }

    @Override
    public LuaEvent silly$getErrorEvent() {
        return ERROR;
    }
}
