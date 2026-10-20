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
    public LuaEvent ERROR = new LuaEvent();

    @Unique
    @LuaWhitelist
    @LuaFieldDoc("events.gui_render")
    public LuaEvent GUI_RENDER = new LuaEvent();

    @Unique
    @LuaWhitelist
    @LuaFieldDoc("events.ping_failed")
    public LuaEvent PING_FAILED = new LuaEvent();

    @Unique
    @LuaWhitelist
    @LuaFieldDoc("events.silly_sound_event")
    public LuaEvent SILLY_SOUND_EVENT = new LuaEvent();

//    @Unique
//    @LuaWhitelist
//    @LuaFieldDoc("events.move")
//    public LuaEvent MOVE;

    @Shadow
    @Final
    private Map<String, LuaEvent> events;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initMixin(CallbackInfo ci) {
        events.put("ERROR", ERROR);
        events.put("GUI_RENDER", GUI_RENDER);
        events.put("PING_FAILED", PING_FAILED);
        events.put("SILLY_SOUND_EVENT", SILLY_SOUND_EVENT);
    }

    @Override
    public LuaEvent silly$getErrorEvent() {
        return ERROR;
    }
}
