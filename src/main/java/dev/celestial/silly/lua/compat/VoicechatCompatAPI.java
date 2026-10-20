package dev.celestial.silly.lua.compat;

import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientPlayerStateManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import dev.celestial.silly.plugin.SillyVoicechatPlugin;
import org.figuramc.figura.lua.FiguraLuaRuntime;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;import org.figuramc.figura.lua.docs.LuaTypeDoc;

import java.util.UUID;

//? if !1.20.4 {
import static de.maxhenkel.voicechat.voice.common.AudioUtils.sampleDb;
//?}

@LuaWhitelist
@LuaTypeDoc(
        value = "silly.compats.svc",
        name = "SillyVoicechatCompatAPI"
)
public class VoicechatCompatAPI extends BaseCompatAPI {
    public VoicechatCompatAPI(FiguraLuaRuntime runtime) {
        super(runtime);
    }
    public ClientVoicechat CLIENT = ClientManager.getClient();
    public ClientPlayerStateManager STATE = ClientManager.getPlayerStateManager();

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.svc.is_muted",
            overloads = {
                    @LuaMethodOverload(returnType = Boolean.class)
            }
    )
    public Boolean isMuted() {
        return avatar.isHost && STATE.isMuted();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.svc.set_muted",
            overloads = {
                    @LuaMethodOverload(
                            argumentNames = { "state" },
                            argumentTypes = { Boolean.class }
                    )
            }
    )
    public void setMuted(@LuaNotNil Boolean state) {
        if (avatar.isHost)
            STATE.setMuted(state);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.svc.set_deafened",
            overloads = {
                    @LuaMethodOverload(
                            argumentNames = { "state" },
                            argumentTypes = { Boolean.class }
                    )
            }
    )
    public void setDeafened(@LuaNotNil Boolean state) {
        if (avatar.isHost)
            STATE.setDisabled(state);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.svc.is_deafened",
            overloads = {
                    @LuaMethodOverload(
                            argumentNames = { "uuid" },
                            argumentTypes = { String.class },
                            returnType = Boolean.class
                    ),
                    @LuaMethodOverload(returnType = Boolean.class)
            }
    )
    public Boolean isDeafened(String uuid) {
        uuid = uuid != null ? uuid : avatar.owner.toString();
        UUID targetUUID = UUID.fromString(uuid);
        var state = STATE.getState(targetUUID);
        return state != null && state.isDisabled();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.svc.is_disconnected",
            overloads = {
                    @LuaMethodOverload(
                            argumentNames = { "uuid" },
                            argumentTypes = { String.class },
                            returnType = Boolean.class
                    ),
                    @LuaMethodOverload(returnType = Boolean.class)
            }
    )
    public Boolean isDisconnected(String uuid) {
        uuid = uuid != null ? uuid : avatar.owner.toString();
        UUID targetUUID = UUID.fromString(uuid);
        var state = STATE.getState(targetUUID);
        return state != null && state.isDisconnected();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.svc.is_talking",
            overloads = {
                    @LuaMethodOverload(
                            argumentNames = { "uuid" },
                            argumentTypes = { String.class },
                            returnType = Boolean.class
                    ),
                    @LuaMethodOverload(returnType = Boolean.class)
            }
    )
    public Boolean isTalking(String uuid) {
        uuid = uuid != null ? uuid : avatar.owner.toString();
        UUID targetUUID = UUID.fromString(uuid);
        return CLIENT.getTalkCache().isTalking(targetUUID);
    }

    private Double transformDb(Double value) {
        return (value + 127.0f) / 127.0f * 100.0f;
    }

    private double transformPcm(double averagePcm) {
        return Math.abs(averagePcm);
    }

    private double getAverageAudioLevel(short[] pcm) {
        long value = 0;

        for(short sample : pcm) {
            value += Math.abs(sample);
        }

        return (value / (double)pcm.length);
    }

    //? if 1.20.4 {
    /*// turns out, 1.20.4 SVC does *NOT* contain this function!
    // fun! -Niko
    private static double sampleDb(short sample) {
        if (sample == 0) {
            return (double)-127.0F;
        } else {
            int mag = Math.abs(sample);
            double norm = (double)mag / (double)32768.0F;
            double db = (double)20.0F * Math.log10(norm);
            if (!Double.isFinite(db)) {
                return (double)-127.0F;
            } else {
                if (db > (double)0.0F) {
                    db = (double)0.0F;
                }

                if (db < (double)-127.0F) {
                    db = (double)-127.0F;
                }

                return db;
            }
        }
    }
    *///?}

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.svc.get_volume",
            overloads = {
                    @LuaMethodOverload(
                            argumentNames = { "uuid" },
                            argumentTypes = { String.class },
                            returnType = Double.class
                    ),
                    @LuaMethodOverload(returnType = Double.class)
            }
    )
    public Double getVolume(String uuid) {
        uuid = uuid != null ? uuid : avatar.owner.toString();
        UUID targetUUID = UUID.fromString(uuid);

        SillyVoicechatPlugin.SillyVoiceData data = SillyVoicechatPlugin.AUDIO.getOrDefault(targetUUID, SillyVoicechatPlugin.emptyData(targetUUID));
        var pcm = data.pcm();
        if (pcm.length == 0) return 0d;
        return transformPcm(
                (System.currentTimeMillis() - data.timestamp()) >= 250L
                        ? 0d
                        : getAverageAudioLevel(pcm)
        );
    }
}
