package dev.celestial.silly.plugin;


import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent;
import de.maxhenkel.voicechat.api.events.ClientSoundEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.voice.common.MicPacket;
import dev.celestial.silly.SillyUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

//? if neoforge {
/*import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
@ForgeVoicechatPlugin
*///?}
public class SillyVoicechatPlugin implements VoicechatPlugin {
    public static VoicechatApi API;
    public static Map<UUID, SillyVoiceData> AUDIO = new ConcurrentHashMap<>();

    @Override
    public String getPluginId() {
        return "silly";
    }

    @Override
    public void initialize(VoicechatApi api) {
        SillyUtil.Devlog("SillyVCPlugin Initialized!");
        API = api;
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        SillyUtil.Devlog("SillyVCPlugin events registered");
        registration.registerEvent(ClientSoundEvent.class, (ev) -> {
            var voiceData = new SillyVoiceData(SillyUtil.clientUUID(), ev.getRawAudio(), System.currentTimeMillis());
            AUDIO.put(voiceData.owner, voiceData);
        });
        registration.registerEvent(ClientReceiveSoundEvent.EntitySound.class, (ev) -> {
            //? if 1.20.4 {
            /*// pray the audio channel ID matches.
            var voiceData = new SillyVoiceData(ev.getId(), ev.getRawAudio(), System.currentTimeMillis());
            *///?} else {
            var voiceData = new SillyVoiceData(ev.getEntityId(), ev.getRawAudio(), System.currentTimeMillis());
            //?}

            AUDIO.put(voiceData.owner, voiceData);
        });
    }

    public static SillyVoiceData emptyData(UUID owner) {
        return new SillyVoiceData(owner, new short[0], 0L);
    }

    public record SillyVoiceData(UUID owner, short[] pcm, long timestamp) {
    }
}
