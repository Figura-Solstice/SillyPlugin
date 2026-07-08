package dev.celestial.silly.lua.compat;

import dev.celestial.silly.SillyEnums;
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

@LuaWhitelist
@LuaTypeDoc(name = "VivecraftCompatAPI", value = "silly.compats.vivecraft")
public class VivecraftCompatAPI extends BaseCompatAPI {
    private final VRClientAPI api;

    public VivecraftCompatAPI(FiguraLuaRuntime runtime) {
        super(runtime);
        this.api = VRClientAPI.instance();
    }

    private VRPose getPose() {
        if (api == null || !api.isVRActive()) return null;
        return api.getPreTickWorldPose();
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

    private VRBodyPartData getBodyPartData(String name) {
        VRPose pose = getPose();
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

    @LuaWhitelist
    @LuaMethodDoc("silly.compats.vivecraft.is_vr_initialized")
    public Boolean isVRInitialized() {
        return api != null && api.isVRInitialized();
    }

    @LuaWhitelist
    @LuaMethodDoc("silly.compats.vivecraft.is_vr_active")
    public Boolean isVRActive() {
        return api != null && api.isVRActive();
    }

    @LuaWhitelist
    @LuaMethodDoc("silly.compats.vivecraft.is_seated")
    public Boolean isSeated() {
        if (api == null) return false;
        return api.isSeated();
    }

    @LuaWhitelist
    @LuaMethodDoc("silly.compats.vivecraft.is_left_handed")
    public Boolean isLeftHanded() {
        if (api == null) return false;
        return api.isLeftHanded();
    }

    @LuaWhitelist
    @LuaMethodDoc("silly.compats.vivecraft.get_world_scale")
    public Float getWorldScale() {
        if (api == null) return 1.0f;
        return api.getWorldScale();
    }

    @LuaWhitelist
    @LuaMethodDoc("silly.compats.vivecraft.get_fbt_mode")
    public String getFBTMode() {
        if (api == null) return "none";
        return api.getFBTMode().name();
    }

    @LuaWhitelist
    @LuaMethodDoc("silly.compats.vivecraft.get_head_pos")
    public FiguraVec3 getHeadPos() {
        VRBodyPartData data = getBodyPartData("head");
        if (data == null) return null;
        return toFigura(data.getPos());
    }

    @LuaWhitelist
    @LuaMethodDoc("silly.compats.vivecraft.get_head_dir")
    public FiguraVec3 getHeadDir() {
        VRBodyPartData data = getBodyPartData("head");
        if (data == null) return null;
        return toFigura(data.getDir());
    }

    @LuaWhitelist
    @LuaMethodDoc("silly.compats.vivecraft.get_head_rot")
    public FiguraVec3 getHeadRot() {
        VRBodyPartData data = getBodyPartData("head");
        if (data == null) return null;
        return toFiguraRot(data);
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
        VRBodyPartData data = getBodyPartData(hand);
        if (data == null) return null;
        return toFigura(data.getPos());
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
        VRBodyPartData data = getBodyPartData(hand);
        if (data == null) return null;
        return toFigura(data.getDir());
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
        VRBodyPartData data = getBodyPartData(hand);
        if (data == null) return null;
        return toFiguraRot(data);
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
        VRBodyPartData data = getBodyPartData(bodyPart);
        if (data == null) return null;
        return toFigura(data.getPos());
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
        VRBodyPartData data = getBodyPartData(bodyPart);
        if (data == null) return null;
        return toFigura(data.getDir());
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
        VRBodyPartData data = getBodyPartData(bodyPart);
        if (data == null) return null;
        return toFiguraRot(data);
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
