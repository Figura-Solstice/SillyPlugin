package dev.celestial.silly.lua;

//? if >1.20.5 {
import net.minecraft.client.sounds.JOrbisAudioStream;
//?} else {
/*import com.mojang.blaze3d.audio.OggAudioStream;
*///?}
import com.mojang.blaze3d.audio.SoundBuffer;
import dev.celestial.silly.SillyPlugin;
import dev.celestial.silly.SillyUtil;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.api.sound.LuaSound;
import org.figuramc.figura.lua.api.sound.SoundAPI;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;

@LuaWhitelist
public class SillyAudioEncoding {
    private final Avatar avatar;
    public SillyAudioEncoding(Avatar owner) {
        this.avatar = owner;
    }

    private static byte[] convertRawData(Object rawData) {
        byte[] data = new byte[0];
        if (rawData instanceof String rawString) {
            try {
                data = Base64.getDecoder().decode(rawString);
            } catch (IllegalArgumentException e) {
                throw new LuaError(e.getMessage());
            }
        } else if (rawData instanceof LuaTable table) {
            data = new byte[table.length()];
            for(int i = 0; i < data.length; i++)
                data[i] = (byte) table.get(i + 1).checkint();
        } else {
            throw new LuaError("Expected base64 or byte array, got " + rawData.getClass().getSimpleName());
        }
        return data;
    }

    @LuaWhitelist
    public static SillySoundDataContainer vorbisToPCM(@LuaNotNil Object rawData) {
        var data = convertRawData(rawData);
        //? if >1.20.5 {
        try (JOrbisAudioStream stream = new JOrbisAudioStream(new ByteArrayInputStream(data))) {
            var raw = stream.readAll();
            var format = stream.getFormat();
            return new SillySoundDataContainer(raw.array(), format);
        } catch (IOException e) {
            throw new LuaError(e.getMessage());
        }
        //?} else {
        /*try (OggAudioStream stream = new OggAudioStream(new ByteArrayInputStream(data))) {
            var raw = stream.readAll();
            var format = stream.getFormat();
            return new SillySoundDataContainer(raw.array(), format);
        } catch (IOException e) {
            throw new LuaError(e.getMessage());
        }
        *///?}
    }

    @LuaWhitelist
    public void newSoundFromPCM(@LuaNotNil String name, @LuaNotNil Object rawData, @LuaNotNil LuaTable format) {
        var data = convertRawData(rawData);
        var aformat = SillySoundDataContainer.formatFromTable(format);
        var buf = ByteBuffer.allocate(data.length*2);
        buf.put(data);
        var soundbuf = new SoundBuffer(buf, aformat);
        this.avatar.customSounds.put(name, soundbuf);
    }

    @LuaWhitelist
    public static class SillySoundDataContainer {
        private final byte[] data;
        private final AudioFormat format;

        public SillySoundDataContainer(byte[] data, AudioFormat format) {
            this.data = data;
            this.format = format;
        }

        @LuaWhitelist
        public byte[] getData() {
            return data;
        }

        @LuaWhitelist
        public LuaTable getFormat() {
            var ret = new LuaTable();
            ret.set("channels", format.getChannels());
            ret.set("framerate", format.getFrameRate());
            ret.set("framesize", format.getFrameSize());
            ret.set("samplerate", format.getSampleRate());
            ret.set("samplesize", format.getSampleSizeInBits());
            ret.set("bigendian", LuaValue.valueOf(format.isBigEndian()));
            ret.set("encoding", format.getEncoding().toString());
            return ret;
        }

        public static AudioFormat formatFromTable(LuaTable input) {
            var channels = input.get("channels").checkint();
            var framerate = input.get("framerate").checkdouble();
            var framesize = input.get("framesize").checkint();
            var samplerate = input.get("samplerate").checkdouble();
            var samplesize = input.get("samplesize").checkint();
            var bigendian = input.get("bigendian").checkboolean();
            var encoding = switch (input.get("encoding").checkjstring()) {
                case "PCM_SIGNED" -> AudioFormat.Encoding.PCM_SIGNED;
                case "PCM_UNSIGNED" -> AudioFormat.Encoding.PCM_UNSIGNED;
                case "PCM_FLOAT" -> AudioFormat.Encoding.PCM_FLOAT;
                case "ULAW" -> AudioFormat.Encoding.ULAW;
                case "ALAW" -> AudioFormat.Encoding.ALAW;
                default -> throw new LuaError("Expected encoding to be one of PCM_SIGNED, PCM_UNSIGNED, PCM_FLOAT, ULAW, ALAW");
            };
            return new AudioFormat(encoding, (float)samplerate, samplesize, channels, framesize, (float)framerate, bigendian);
        }
    }
}
