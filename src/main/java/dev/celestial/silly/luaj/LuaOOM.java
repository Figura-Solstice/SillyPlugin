package dev.celestial.silly.luaj;

public class LuaOOM extends LuaUncatchableError {
    public LuaOOM() { super(); }
    public LuaOOM(String message) { super(message); }
    public LuaOOM(Throwable cause) { super(cause); }
    public LuaOOM(String message, Throwable cause) { super(message, cause); }
}
