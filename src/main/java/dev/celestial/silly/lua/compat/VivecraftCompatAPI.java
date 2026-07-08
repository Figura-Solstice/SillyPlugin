package dev.celestial.silly.lua.compat;

import dev.celestial.silly.SillyEnums;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.figuramc.figura.lua.FiguraLuaRuntime;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.vivecraft.api.client.VRClientAPI;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.client.ClientVRPlayers;

import java.util.UUID;

@LuaWhitelist
@LuaTypeDoc(name = "VivecraftCompatAPI", value = "silly.compats.vivecraft")
public class VivecraftCompatAPI extends BaseCompatAPI {
    private final VRClientAPI api;
    private final ClientVRPlayers players;
    private final UUID ownerUuid;

    public VivecraftCompatAPI(FiguraLuaRuntime runtime) {
        super(runtime);
        this.api = VRClientAPI.instance();
        this.players = ClientVRPlayers.getInstance();
        this.ownerUuid = runtime.owner.owner;
    }

    private VRPose getOwnerPose() {
        if (players == null) return null;
        ClientVRPlayers.RotInfo info = players.getLatestRotationsForPlayer(ownerUuid);
        if (info == null) return null;
        Vec3 offset = Vec3.ZERO;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            Player player = minecraft.level.getPlayerByUUID(ownerUuid);
            if (player != null) offset = player.position();
        }
        return info.asVRPose(offset);
    }

    private VRPose getHostPose() {
        if (api == null || !api.isVRActive()) return null;
        return api.getPreTickWorldPose();
    }

    private VRPose getPose(boolean host) {
        return host ? getHostPose() : getOwnerPose();
    }

    private VRBodyPart parseBodyPart(String name) {
        return switch (name.toLowerCase()) {
            case "head" -> VRBodyPart.HEAD;
            case "main_hand", "mainhand", "main" -> VRBodyPart.MAIN_HAND;
            case "off_hand", "offhand", "off" -> VRBodyPart.OFF_HAND;
            case "left_foot", "leftfoot" -> VRBodyPart.LEFT_FOOT;
            case "right_foot", "rightfoot" -> VRBodyPart.RIGHT_FOOT;
            case "waist" -> VRBodyPart.WAIST;
            case "left_knee", "leftknee" -> VRBodyPart.LEFT_KNEE;
            case "right_knee", "rightknee" -> VRBodyPart.RIGHT_KNEE;
            case "left_elbow", "leftelbow" -> VRBodyPart.LEFT_ELBOW;
            case "right_elbow", "rightelbow" -> VRBodyPart.RIGHT_ELBOW;
            default -> null;
        };
    }

    private VRBodyPartData getBodyPartData(String name, boolean host) {
        VRPose pose = getPose(host);
        if (pose == null) return null;
        VRBodyPart part = parseBodyPart(name);
        if (part == null) return null;
        return pose.getBodyPartData(part);
    }

    private static FiguraVec3 toFigura(Vec3 vec) {
        return FiguraVec3.of(vec.x, vec.y, vec.z);
    }

    private static FiguraVec3 toFiguraRot(VRBodyPartData data) {
        return FiguraVec3.of(data.getPitch(), data.getYaw(), data.getRoll());
    }

    private Boolean isVRActive(boolean host) {
        if (host) return api != null && api.isVRActive();
        if (players == null) return false;
        return players.isVRPlayer(ownerUuid);
    }

    private Boolean isSeated(boolean host) {
        if (host) {
            if (api == null) return false;
            return api.isSeated();
        }
        if (players == null) return false;
        return players.isVRAndSeated(ownerUuid);
    }

    private Boolean isLeftHanded(boolean host) {
        if (host) {
            if (api == null) return false;
            return api.isLeftHanded();
        }
        if (players == null) return false;
        return players.isVRAndLeftHanded(ownerUuid);
    }

    private Float getWorldScale(boolean host) {
        if (host) {
            if (api == null) return 1.0f;
            return api.getWorldScale();
        }
        if (players == null) return 1.0f;
        ClientVRPlayers.RotInfo info = players.getLatestRotationsForPlayer(ownerUuid);
        if (info == null) return 1.0f;
        return info.worldScale;
    }

    private String getFBTMode(boolean host) {
        if (host) {
            if (api == null) return "none";
            return api.getFBTMode().name();
        }
        if (players == null) return "none";
        ClientVRPlayers.RotInfo info = players.getLatestRotationsForPlayer(ownerUuid);
        if (info == null) return "none";
        return info.fbtMode.name();
    }

    private FiguraVec3 getHeadPos(boolean host) {
        VRBodyPartData data = getBodyPartData("head", host);
        if (data == null) return null;
        return toFigura(data.getPos());
    }

    private FiguraVec3 getHeadDir(boolean host) {
        VRBodyPartData data = getBodyPartData("head", host);
        if (data == null) return null;
        return toFigura(data.getDir());
    }

    private FiguraVec3 getHeadRot(boolean host) {
        VRBodyPartData data = getBodyPartData("head", host);
        if (data == null) return null;
        return toFiguraRot(data);
    }

    private FiguraVec3 getHandPos(String hand, boolean host) {
        VRBodyPartData data = getBodyPartData(hand, host);
        if (data == null) return null;
        return toFigura(data.getPos());
    }

    private FiguraVec3 getHandDir(String hand, boolean host) {
        VRBodyPartData data = getBodyPartData(hand, host);
        if (data == null) return null;
        return toFigura(data.getDir());
    }

    private FiguraVec3 getHandRot(String hand, boolean host) {
        VRBodyPartData data = getBodyPartData(hand, host);
        if (data == null) return null;
        return toFiguraRot(data);
    }

    private FiguraVec3 getBodyPartPos(String bodyPart, boolean host) {
        VRBodyPartData data = getBodyPartData(bodyPart, host);
        if (data == null) return null;
        return toFigura(data.getPos());
    }

    private FiguraVec3 getBodyPartDir(String bodyPart, boolean host) {
        VRBodyPartData data = getBodyPartData(bodyPart, host);
        if (data == null) return null;
        return toFigura(data.getDir());
    }

    private FiguraVec3 getBodyPartRot(String bodyPart, boolean host) {
        VRBodyPartData data = getBodyPartData(bodyPart, host);
        if (data == null) return null;
        return toFiguraRot(data);
    }

    @LuaWhitelist
    @LuaMethodDoc("silly.compats.vivecraft.is_vr_initialized")
    public Boolean isVRInitialized() {
        return api != null && api.isVRInitialized();
    }

    @LuaWhitelist
    @LuaMethodDoc("silly.compats.vivecraft.is_vr_active")
    public Boolean isVRActive() {
        return isVRActive(false);
    }

    @LuaWhitelist
    @LuaMethodDoc("silly.compats.vivecraft.is_vr_active_host")
    public Boolean isVRActiveHost() {
        return isVRActive(true);
    }

    @LuaWhitelist
    @LuaMethodDoc("silly.compats.vivecraft.is_seated")
    public Boolean isSeated() {
        return isSeated(false);
    }

    @LuaWhitelist
    @LuaMethodDoc("silly.compats.vivecraft.is_seated_host")
    public Boolean isSeatedHost() {
        return isSeated(true);
    }

    @LuaWhitelist
    @LuaMethodDoc("silly.compats.vivecraft.is_left_handed")
    public Boolean isLeftHanded() {
        return isLeftHanded(false);
    }

    @LuaWhitelist
    @LuaMethodDoc("silly.compats.vivecraft.is_left_handed_host")
    public Boolean isLeftHandedHost() {
        return isLeftHanded(true);
    }

    @LuaWhitelist
    @LuaMethodDoc("silly.compats.vivecraft.get_world_scale")
    public Float getWorldScale() {
        return getWorldScale(false);
    }

    @LuaWhitelist
    @LuaMethodDoc("silly.compats.vivecraft.get_world_scale_host")
    public Float getWorldScaleHost() {
        return getWorldScale(true);
    }

    @LuaWhitelist
    @LuaMethodDoc("silly.compats.vivecraft.get_fbt_mode")
    public String getFBTMode() {
        return getFBTMode(false);
    }

    @LuaWhitelist
    @LuaMethodDoc("silly.compats.vivecraft.get_fbt_mode_host")
    public String getFBTModeHost() {
        return getFBTMode(true);
    }

    @LuaWhitelist
    @LuaMethodDoc("silly.compats.vivecraft.get_head_pos")
    public FiguraVec3 getHeadPos() {
        return getHeadPos(false);
    }

    @LuaWhitelist
    @LuaMethodDoc("silly.compats.vivecraft.get_head_pos_host")
    public FiguraVec3 getHeadPosHost() {
        return getHeadPos(true);
    }

    @LuaWhitelist
    @LuaMethodDoc("silly.compats.vivecraft.get_head_dir")
    public FiguraVec3 getHeadDir() {
        return getHeadDir(false);
    }

    @LuaWhitelist
    @LuaMethodDoc("silly.compats.vivecraft.get_head_dir_host")
    public FiguraVec3 getHeadDirHost() {
        return getHeadDir(true);
    }

    @LuaWhitelist
    @LuaMethodDoc("silly.compats.vivecraft.get_head_rot")
    public FiguraVec3 getHeadRot() {
        return getHeadRot(false);
    }

    @LuaWhitelist
    @LuaMethodDoc("silly.compats.vivecraft.get_head_rot_host")
    public FiguraVec3 getHeadRotHost() {
        return getHeadRot(true);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.compats.vivecraft.get_hand_pos",
            overloads = @LuaMethodOverload(
                    argumentTypes = SillyEnums.VR_BODY_PART.class,
                    argumentNames = "hand"
            )
    )
    public FiguraVec3 getHandPos(@LuaNotNil String hand) {
        return getHandPos(hand, false);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.compats.vivecraft.get_hand_pos_host",
            overloads = @LuaMethodOverload(
                    argumentTypes = SillyEnums.VR_BODY_PART.class,
                    argumentNames = "hand"
            )
    )
    public FiguraVec3 getHandPosHost(@LuaNotNil String hand) {
        return getHandPos(hand, true);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.compats.vivecraft.get_hand_dir",
            overloads = @LuaMethodOverload(
                    argumentTypes = SillyEnums.VR_BODY_PART.class,
                    argumentNames = "hand"
            )
    )
    public FiguraVec3 getHandDir(@LuaNotNil String hand) {
        return getHandDir(hand, false);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.compats.vivecraft.get_hand_dir_host",
            overloads = @LuaMethodOverload(
                    argumentTypes = SillyEnums.VR_BODY_PART.class,
                    argumentNames = "hand"
            )
    )
    public FiguraVec3 getHandDirHost(@LuaNotNil String hand) {
        return getHandDir(hand, true);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.compats.vivecraft.get_hand_rot",
            overloads = @LuaMethodOverload(
                    argumentTypes = SillyEnums.VR_BODY_PART.class,
                    argumentNames = "hand"
            )
    )
    public FiguraVec3 getHandRot(@LuaNotNil String hand) {
        return getHandRot(hand, false);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.compats.vivecraft.get_hand_rot_host",
            overloads = @LuaMethodOverload(
                    argumentTypes = SillyEnums.VR_BODY_PART.class,
                    argumentNames = "hand"
            )
    )
    public FiguraVec3 getHandRotHost(@LuaNotNil String hand) {
        return getHandRot(hand, true);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.compats.vivecraft.get_body_part_pos",
            overloads = @LuaMethodOverload(
                    argumentTypes = SillyEnums.VR_BODY_PART.class,
                    argumentNames = "bodyPart"
            )
    )
    public FiguraVec3 getBodyPartPos(@LuaNotNil String bodyPart) {
        return getBodyPartPos(bodyPart, false);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.compats.vivecraft.get_body_part_pos_host",
            overloads = @LuaMethodOverload(
                    argumentTypes = SillyEnums.VR_BODY_PART.class,
                    argumentNames = "bodyPart"
            )
    )
    public FiguraVec3 getBodyPartPosHost(@LuaNotNil String bodyPart) {
        return getBodyPartPos(bodyPart, true);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.compats.vivecraft.get_body_part_dir",
            overloads = @LuaMethodOverload(
                    argumentTypes = SillyEnums.VR_BODY_PART.class,
                    argumentNames = "bodyPart"
            )
    )
    public FiguraVec3 getBodyPartDir(@LuaNotNil String bodyPart) {
        return getBodyPartDir(bodyPart, false);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.compats.vivecraft.get_body_part_dir_host",
            overloads = @LuaMethodOverload(
                    argumentTypes = SillyEnums.VR_BODY_PART.class,
                    argumentNames = "bodyPart"
            )
    )
    public FiguraVec3 getBodyPartDirHost(@LuaNotNil String bodyPart) {
        return getBodyPartDir(bodyPart, true);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.compats.vivecraft.get_body_part_rot",
            overloads = @LuaMethodOverload(
                    argumentTypes = SillyEnums.VR_BODY_PART.class,
                    argumentNames = "bodyPart"
            )
    )
    public FiguraVec3 getBodyPartRot(@LuaNotNil String bodyPart) {
        return getBodyPartRot(bodyPart, false);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.compats.vivecraft.get_body_part_rot_host",
            overloads = @LuaMethodOverload(
                    argumentTypes = SillyEnums.VR_BODY_PART.class,
                    argumentNames = "bodyPart"
            )
    )
    public FiguraVec3 getBodyPartRotHost(@LuaNotNil String bodyPart) {
        return getBodyPartRot(bodyPart, true);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.compats.vivecraft.trigger_haptic",
            overloads = @LuaMethodOverload(
                    argumentTypes = {SillyEnums.VR_BODY_PART.class, Float.class, Float.class, Float.class, Float.class},
                    argumentNames = {"bodyPart", "duration", "frequency", "amplitude", "delay"}
            )
    )
    public void triggerHaptic(@LuaNotNil String bodyPart, Float duration, Float frequency, Float amplitude, Float delay) {
        if (api == null || !api.isVRActive()) return;
        VRBodyPart part = parseBodyPart(bodyPart);
        if (part == null) return;
        float dur = duration != null ? duration : 0.1f;
        float freq = frequency != null ? frequency : 160f;
        float amp = amplitude != null ? amplitude : 1.0f;
        float dly = delay != null ? delay : 0f;
        api.triggerHapticPulse(part, dur, freq, amp, dly);
    }
}
