package dev.celestial.silly.helper;

import dev.celestial.silly.lua.BackportsAPI;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class CallerContext implements AutoCloseable {
    private UUID uuid;
    @Nullable
    private UUID owner;
    private String context;

    public CallerContext(UUID uuid, @Nullable UUID owner, String ctx) {
        this.uuid = uuid;
        this.owner = owner;
        this.context = ctx;
        BackportsAPI.pushStack(this.uuid, this.context);
        if (this.owner != null)
            BackportsAPI.pushStack(this.owner, this.context + "/owner");
    }

    public static CallerContext Open(UUID uuid, UUID owner, String context) {
        return new CallerContext(uuid, owner, context);
    }

    @Override
    public void close() throws IllegalStateException {
        if (this.owner != null)
            BackportsAPI.popStack(this.owner, this.context + "/owner");
        BackportsAPI.popStack(this.uuid, this.context);
    }
}
