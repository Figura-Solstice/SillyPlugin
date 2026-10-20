package dev.celestial.silly.luaj.memory;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

// https://github.com/FiguraMC/memory-tracker
public interface AllocationTracker<E extends Throwable> {
    public static final int
            OBJECT_SIZE = 8, // Object overhead
            REFERENCE_SIZE = 4, // Assume compacted references
            LONG_SIZE = 8, INT_SIZE = 4, SHORT_SIZE = 2, CHAR_SIZE = 2, BYTE_SIZE = 1, BOOLEAN_SIZE = 1,
            DOUBLE_SIZE = 8, FLOAT_SIZE = 4,
            VEC2_SIZE = OBJECT_SIZE + DOUBLE_SIZE * 2,
            VEC3_SIZE = OBJECT_SIZE + DOUBLE_SIZE * 3,
            VEC4_SIZE = OBJECT_SIZE + DOUBLE_SIZE * 4,
            MAT2_SIZE = OBJECT_SIZE + DOUBLE_SIZE * 4,
            MAT3_SIZE = OBJECT_SIZE + DOUBLE_SIZE * 9,
            MAT4_SIZE = OBJECT_SIZE + DOUBLE_SIZE * 16;

    void track(byte @NotNull [] byteArray) throws E;
    void track(int @NotNull [] intArray) throws E;
    void track(float @NotNull [] floatArray) throws E;
    void track(Object @NotNull [] objectArray) throws E;
    void track(@NotNull String string) throws E;

    long getTotal();
    Map<String, Integer> findCulprits();

    State<E> track(@NotNull Object obj, int amount) throws E;

    interface State<E extends Throwable> extends Runnable {
        void changeSize(int change) throws E;
    }
    enum ErrorReason {
        OVER_GRACE_MAXIMUM,
        GRACE_EXPIRED,
    }
}
