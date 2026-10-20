package dev.celestial.silly.lua.data;

import com.mojang.blaze3d.audio.SoundBuffer;
import dev.celestial.silly.mixin.SoundBufferAccessor;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.jetbrains.annotations.Nullable;

@LuaWhitelist
@LuaTypeDoc(name = "SillySoundEventData", value = "silly.snd")
public class SillySoundEventData {
    @LuaWhitelist
    @LuaFieldDoc("silly.snd.id")
    public String id;
    @LuaWhitelist
    @LuaFieldDoc("silly.snd.subtitle")
    public Component subtitle;
    @LuaWhitelist
    @LuaFieldDoc("silly.snd.volume")
    public float volume;
    @LuaWhitelist
    @LuaFieldDoc("silly.snd.pitch")
    public float pitch;
    @LuaWhitelist
    @LuaFieldDoc("silly.snd.pos")
    public FiguraVec3 pos;
    @LuaWhitelist
    @LuaFieldDoc("silly.snd.looping")
    public boolean looping;
    @LuaWhitelist
    @LuaFieldDoc("silly.snd.channels")
    public int channels;
    @LuaWhitelist
    @LuaFieldDoc("silly.snd.owner")
    public @Nullable String owner;
    @LuaWhitelist
    @LuaFieldDoc("silly.snd.category")
    public String category;
    @LuaWhitelist
    @LuaFieldDoc("silly.snd.file")
    public @Nullable String file;
    @LuaWhitelist
    @LuaFieldDoc("silly.snd.cancelled")
    public boolean cancelled = false;

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.snd.cancel"
    )
    public void cancel() {
        cancelled = true;
    }

    @LuaWhitelist
    public Object __index(String query) {
        return switch (query) {
            case "id" -> id;
            case "subtitle" -> subtitle;
            case "volume" -> volume;
            case "pitch" -> pitch;
            case "pos" -> pos;
            case "looping" -> looping;
            case "channels" -> channels;
            case "owner" -> owner;
            case "category" -> category;
            case "file" -> file;
            case "cancelled" -> cancelled;
            default -> null;
        };
    }

    public void fillFromSoundBuffer(SoundBuffer buf) {
        var accessor = ((SoundBufferAccessor)buf);
        var format = accessor.silly$getFormat();
        this.channels = format.getChannels();
    }
}
