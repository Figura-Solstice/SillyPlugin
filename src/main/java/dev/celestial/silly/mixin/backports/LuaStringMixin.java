package dev.celestial.silly.mixin.backports;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.luaj.vm2.LuaString;
import org.spongepowered.asm.mixin.*;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;


// this entire mixin is just a port of https://github.com/FiguraMC/luaj/pull/4/
// all credit goes to PenguinEncounter
@Mixin(value = LuaString.class, remap = false)
public class LuaStringMixin {
    @Shadow
    @Final
    public byte[] m_bytes;
    @Shadow
    @Final
    public int m_offset;
    @Shadow
    @Final
    public int m_length;
    @Unique
    private String silly$cachedUTFDecodeString;

    @WrapMethod(method = "tojstring")
    public String silly$fixUTF8Decode(Operation<String> original) {
        if (silly$cachedUTFDecodeString != null) return silly$cachedUTFDecodeString;
        return original.call();
    }

    /**
     * @author NikoSolstice
     * @reason see <a href="https://github.com/FiguraMC/luaj/pull/4/">FiguraMC/luaj#4</a> by PenguinEncounter
     */
    @Overwrite
    public static String decodeAsUtf8(byte[] bytes, int offset, int length) {
        byte[] subset = Arrays.copyOfRange(bytes, offset, offset + length);
        return new String(subset, StandardCharsets.UTF_8);
    }

    /**
     * @author NikoSolstice
     * @reason see <a href="https://github.com/FiguraMC/luaj/pull/4/">FiguraMC/luaj#4</a> by PenguinEncounter
     */
    @Overwrite
    public static int encodeToUtf8(char[] chars, int nchars, byte[] bytes, int off) {
        char[] subset = Arrays.copyOfRange(chars, 0, nchars);
        CharBuffer buf = CharBuffer.wrap(subset);
        ByteBuffer outBuf = StandardCharsets.UTF_8.encode(buf);
        byte[] result = Arrays.copyOfRange(outBuf.array(), outBuf.position(), outBuf.limit());
        System.arraycopy(result, 0, bytes, off, result.length);
        return result.length;
    }

    @Unique
    private static final CharsetDecoder silly$utf8Validator = StandardCharsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT);

    @Unique
    private Boolean silly$isUTF8Ok = null;

    /**
     * @author NikoSolstice
     * @reason see <a href="https://github.com/FiguraMC/luaj/pull/4/">FiguraMC/luaj#4</a> by PenguinEncounter
     */
    @Overwrite
    public boolean isValidUtf8() {
        // Strings are immutable* so we can cache this
        if (silly$isUTF8Ok != null) return silly$isUTF8Ok;
        try {
            CharBuffer result = silly$utf8Validator.decode(ByteBuffer.wrap(Arrays.copyOfRange(this.m_bytes, this.m_offset, this.m_offset + this.m_length)));
            silly$cachedUTFDecodeString = result.toString();
            silly$isUTF8Ok = true;
            return true;
        } catch (CharacterCodingException ignored) {
            silly$isUTF8Ok = false;
        }
        return false;
    }
}
