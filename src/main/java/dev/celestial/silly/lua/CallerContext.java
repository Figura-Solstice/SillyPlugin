package dev.celestial.silly.lua;

import java.util.UUID;

public class CallerContext implements AutoCloseable {
    private UUID uuid;
    private String context;

    public CallerContext(UUID uuid, String ctx) {
        this.uuid = uuid;
        this.context = ctx;
        BackportsAPI.pushStack(this.uuid, this.context);
    }

    public static CallerContext Open(UUID uuid, String context) {
        return new CallerContext(uuid, context);
    }

    @Override
    public void close() throws IllegalStateException {
        BackportsAPI.popStack(this.uuid, this.context);
    }
}
