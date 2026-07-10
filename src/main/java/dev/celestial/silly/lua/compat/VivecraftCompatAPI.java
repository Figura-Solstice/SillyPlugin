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
import org.figuramc.figura.math.vector.FiguraVec4;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.vivecraft.api.client.VRClientAPI;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.client.ClientVRPlayers;

import dev.celestial.silly.lua.SillyAPI;
import dev.celestial.silly.not_a_mixin.AvatarExtensions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LuaWhitelist
@LuaTypeDoc(name = "VivecraftCompatAPI", value = "silly.compats.vivecraft")
public class VivecraftCompatAPI extends BaseCompatAPI {
    private final VRClientAPI api;
    private final ClientVRPlayers players;
    private boolean vivecraftAnim = true;

    public VivecraftCompatAPI(FiguraLuaRuntime runtime) {
        super(runtime);
        this.api = VRClientAPI.instance();
        this.players = ClientVRPlayers.getInstance();
    }

    private UUID getTargetUUID(String uuid) {
        return uuid != null ? UUID.fromString(uuid) : avatar.owner;
    }

    private VRPose getPose(String uuid) {
        if (players == null) return null;
        UUID target = getTargetUUID(uuid);
        ClientVRPlayers.RotInfo info = players.getRotationsForPlayer(target);
        if (info == null) return null;
        Vec3 offset = Vec3.ZERO;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            Player player = minecraft.level.getPlayerByUUID(target);
            if (player != null) offset = player.position();
        }
        return info.asVRPose(offset);
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

    private VRBodyPartData getBodyPartData(String name, String uuid) {
        VRPose pose = getPose(uuid);
        if (pose == null) return null;
        VRBodyPart part = parseBodyPart(name);
        if (part == null) return null;
        return pose.getBodyPartData(part);
    }

    private static FiguraVec3 toFigura(Vec3 vec) {
        return FiguraVec3.of(vec.x, vec.y, vec.z);
    }

    private static FiguraVec3 toFiguraRot(VRBodyPartData data) {
        Quaternionfc rot = data.getRotation();
        Vector3f euler = new Vector3f();
        euler.x = org.joml.Math.atan2(rot.y() * rot.z() + rot.w() * rot.x(),
                0.5f - rot.x() * rot.x() - rot.y() * rot.y());
        euler.y = org.joml.Math.safeAsin(-2.0f * (rot.x() * rot.z() - rot.w() * rot.y()));
        euler.z = org.joml.Math.atan2(rot.x() * rot.y() + rot.w() * rot.z(),
                0.5f - rot.y() * rot.y() - rot.z() * rot.z());
        return FiguraVec3.of(-euler.x, euler.y - Math.PI, euler.z).toDeg();
    }

    @LuaWhitelist
    @LuaMethodDoc("silly.compats.vivecraft.is_vr_initialized")
    public Boolean isVRInitialized() {
        return api != null && api.isVRInitialized();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.compats.vivecraft.is_vr_active",
            overloads = {
                    @LuaMethodOverload(returnType = Boolean.class),
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "uuid",
                            returnType = Boolean.class
                    )
            }
    )
    public Boolean isVRActive(String uuid) {
        if (players == null) return false;
        return players.isVRPlayer(getTargetUUID(uuid));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.compats.vivecraft.is_seated",
            overloads = {
                    @LuaMethodOverload(returnType = Boolean.class),
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "uuid",
                            returnType = Boolean.class
                    )
            }
    )
    public Boolean isSeated(String uuid) {
        if (players == null) return false;
        return players.isVRAndSeated(getTargetUUID(uuid));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.compats.vivecraft.is_left_handed",
            overloads = {
                    @LuaMethodOverload(returnType = Boolean.class),
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "uuid",
                            returnType = Boolean.class
                    )
            }
    )
    public Boolean isLeftHanded(String uuid) {
        if (players == null) return false;
        return players.isVRAndLeftHanded(getTargetUUID(uuid));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.compats.vivecraft.get_world_scale",
            overloads = {
                    @LuaMethodOverload(returnType = Float.class),
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "uuid",
                            returnType = Float.class
                    )
            }
    )
    public Float getWorldScale(String uuid) {
        if (players == null) return 1.0f;
        ClientVRPlayers.RotInfo info = players.getRotationsForPlayer(getTargetUUID(uuid));
        if (info == null) return 1.0f;
        return info.worldScale;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.compats.vivecraft.get_fbt_mode",
            overloads = {
                    @LuaMethodOverload(returnType = String.class),
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "uuid",
                            returnType = String.class
                    )
            }
    )
    public String getFBTMode(String uuid) {
        if (players == null) return "none";
        ClientVRPlayers.RotInfo info = players.getRotationsForPlayer(getTargetUUID(uuid));
        if (info == null) return "none";
        return info.fbtMode.name();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.compats.vivecraft.get_head_pos",
            overloads = {
                    @LuaMethodOverload(returnType = FiguraVec3.class),
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "uuid",
                            returnType = FiguraVec3.class
                    )
            }
    )
    public FiguraVec3 getHeadPos(String uuid) {
        VRBodyPartData data = getBodyPartData("head", uuid);
        if (data == null) return null;
        return toFigura(data.getPos());
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.compats.vivecraft.get_head_dir",
            overloads = {
                    @LuaMethodOverload(returnType = FiguraVec3.class),
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "uuid",
                            returnType = FiguraVec3.class
                    )
            }
    )
    public FiguraVec3 getHeadDir(String uuid) {
        VRBodyPartData data = getBodyPartData("head", uuid);
        if (data == null) return null;
        return toFigura(data.getDir());
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.compats.vivecraft.get_head_rot",
            overloads = {
                    @LuaMethodOverload(returnType = FiguraVec3.class),
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "uuid",
                            returnType = FiguraVec3.class
                    )
            }
    )
    public FiguraVec3 getHeadRot(String uuid) {
        VRBodyPartData data = getBodyPartData("head", uuid);
        if (data == null) return null;
        return toFiguraRot(data);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.compats.vivecraft.get_hand_pos",
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = SillyEnums.VR_BODY_PART.class,
                            argumentNames = "hand"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {SillyEnums.VR_BODY_PART.class, String.class},
                            argumentNames = {"hand", "uuid"}
                    )
            }
    )
    public FiguraVec3 getHandPos(@LuaNotNil String hand, String uuid) {
        VRBodyPartData data = getBodyPartData(hand, uuid);
        if (data == null) return null;
        return toFigura(data.getPos());
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.compats.vivecraft.get_hand_dir",
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = SillyEnums.VR_BODY_PART.class,
                            argumentNames = "hand"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {SillyEnums.VR_BODY_PART.class, String.class},
                            argumentNames = {"hand", "uuid"}
                    )
            }
    )
    public FiguraVec3 getHandDir(@LuaNotNil String hand, String uuid) {
        VRBodyPartData data = getBodyPartData(hand, uuid);
        if (data == null) return null;
        return toFigura(data.getDir());
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.compats.vivecraft.get_hand_rot",
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = SillyEnums.VR_BODY_PART.class,
                            argumentNames = "hand"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {SillyEnums.VR_BODY_PART.class, String.class},
                            argumentNames = {"hand", "uuid"}
                    )
            }
    )
    public FiguraVec3 getHandRot(@LuaNotNil String hand, String uuid) {
        VRBodyPartData data = getBodyPartData(hand, uuid);
        if (data == null) return null;
        return toFiguraRot(data);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.compats.vivecraft.get_body_part_pos",
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = SillyEnums.VR_BODY_PART.class,
                            argumentNames = "bodyPart"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {SillyEnums.VR_BODY_PART.class, String.class},
                            argumentNames = {"bodyPart", "uuid"}
                    )
            }
    )
    public FiguraVec3 getBodyPartPos(@LuaNotNil String bodyPart, String uuid) {
        VRBodyPartData data = getBodyPartData(bodyPart, uuid);
        if (data == null) return null;
        return toFigura(data.getPos());
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.compats.vivecraft.get_body_part_dir",
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = SillyEnums.VR_BODY_PART.class,
                            argumentNames = "bodyPart"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {SillyEnums.VR_BODY_PART.class, String.class},
                            argumentNames = {"bodyPart", "uuid"}
                    )
            }
    )
    public FiguraVec3 getBodyPartDir(@LuaNotNil String bodyPart, String uuid) {
        VRBodyPartData data = getBodyPartData(bodyPart, uuid);
        if (data == null) return null;
        return toFigura(data.getDir());
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.compats.vivecraft.get_body_part_rot",
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = SillyEnums.VR_BODY_PART.class,
                            argumentNames = "bodyPart"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {SillyEnums.VR_BODY_PART.class, String.class},
                            argumentNames = {"bodyPart", "uuid"}
                    )
            }
    )
    public FiguraVec3 getBodyPartRot(@LuaNotNil String bodyPart, String uuid) {
        VRBodyPartData data = getBodyPartData(bodyPart, uuid);
        if (data == null) return null;
        return toFiguraRot(data);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.compats.vivecraft.get_pose_data",
            overloads = {
                    @LuaMethodOverload(returnType = Map.class),
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "uuid",
                            returnType = Map.class
                    )
            }
    )
    public Map<String, Object> getPoseData(String uuid) {
        VRPose pose = getPose(uuid);
        if (pose == null) return null;
        Map<String, Object> data = new HashMap<>();
        data.put("fbtMode", pose.getFBTMode().name());
        data.put("leftHanded", pose.isLeftHanded());
        data.put("seated", pose.isSeated());
        for (VRBodyPart part : VRBodyPart.values()) {
            if (!part.availableInMode(pose.getFBTMode())) continue;
            Map<String, Object> partData = new HashMap<>();
            VRBodyPartData bodyData = pose.getBodyPartData(part);
            partData.put("pos", toFigura(bodyData.getPos()));
            partData.put("rot", toFiguraRot(bodyData));
            partData.put("dir", toFigura(bodyData.getDir()));
            Quaternionfc q = bodyData.getRotation();
            partData.put("rot4", FiguraVec4.of(q.x(), q.y(), q.z(), q.w()));
            data.put(part.name().toLowerCase(), partData);
        }
        return data;
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
        if (!avatar.isHost) return;
        if (api == null || !api.isVRActive()) return;
        VRBodyPart part = parseBodyPart(bodyPart);
        if (part == null) return;
        float dur = duration != null ? duration : 0.1f;
        float freq = frequency != null ? frequency : 160f;
        float amp = amplitude != null ? amplitude : 1.0f;
        float dly = delay != null ? delay : 0f;
        api.triggerHapticPulse(part, dur, freq, amp, dly);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "silly.compats.vivecraft.set_vivecraft_anim",
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "value"
            )
    )
    public void setVivecraftAnim(@LuaNotNil Boolean value) {
        this.vivecraftAnim = value;
    }

    @LuaWhitelist
    @LuaMethodDoc("silly.compats.vivecraft.get_vivecraft_anim")
    public Boolean getVivecraftAnim() {
        return vivecraftAnim;
    }

    public static boolean shouldDoVivecraftAnim(UUID uuid) {
        if (uuid == null) return true;
        if (AvatarManager.panic) return true;
        Avatar avatar = AvatarManager.getAvatarForPlayer(uuid);
        if (avatar == null) return true;
        SillyAPI silly = ((AvatarExtensions) avatar).silly$getSilly();
        if (silly == null || silly.compats == null) return true;
        Object api = silly.compats.vivecraft;
        if (!(api instanceof VivecraftCompatAPI vivecraft)) return true;
        return vivecraft.vivecraftAnim;
    }
}
