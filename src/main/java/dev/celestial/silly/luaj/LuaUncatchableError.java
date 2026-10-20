package dev.celestial.silly.luaj;

public class LuaUncatchableError extends Throwable {
    public LuaUncatchableError() { super(); }
    public LuaUncatchableError(String message) { super(message); }
    public LuaUncatchableError(Throwable cause) { super(cause); }
    public LuaUncatchableError(String message, Throwable cause) { super(message, cause); }
}
