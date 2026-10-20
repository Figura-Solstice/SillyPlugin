package dev.celestial.silly;

import org.figuramc.figura.permissions.Permissions;

import java.util.Collection;
import java.util.List;

public class SillyPermissions {
    public static Permissions BUMPSCOCITY = new Permissions("BUMPSCOCITY", 0, 1000, 0, 0, 0, 0, 0);
    public static Permissions FAKE_BLOCKS = new Permissions("FAKE_BLOCKS", 0, 0, 0, 0, 0);
    public static Permissions PRINT = new Permissions("PRINT", 0, 1,1,1,1);
    public static Permissions STEREO_SOUNDS = new Permissions("STEREO_SOUNDS", 0, 1,1,1,1);
    public static Permissions SCRIPT_EXEC = new Permissions("SCRIPT_EXEC", 0, 2, 0, 2, 2, 2, 2);
    public static Permissions EXEC_TIME = new Permissions("EXEC_TIME", 0, 1000 * 60, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    // 512mb
    public static Permissions MEMORY = new Permissions("MEMORY", 0, 512 * 1024, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    public static final Collection<Permissions> PERMISSIONS = List.of(BUMPSCOCITY, FAKE_BLOCKS, SCRIPT_EXEC, EXEC_TIME, PRINT, STEREO_SOUNDS, MEMORY);
}
