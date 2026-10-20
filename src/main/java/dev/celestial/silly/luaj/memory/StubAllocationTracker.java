package dev.celestial.silly.luaj.memory;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class StubAllocationTracker<E extends Throwable> implements AllocationTracker<E> {
    @Override
    public void track(byte @NotNull [] byteArray) throws E {

    }

    @Override
    public void track(int @NotNull [] intArray) throws E {

    }

    @Override
    public void track(float @NotNull [] floatArray) throws E {

    }

    @Override
    public void track(Object @NotNull [] objectArray) throws E {

    }

    @Override
    public void track(@NotNull String string) throws E {

    }

    @Override
    public long getTotal() {
        return 0;
    }

    @Override
    public Map<String, Integer> findCulprits() {
        return Map.of();
    }

    @Override
    public State track(@NotNull Object obj, int amount) throws E {
        return new State();
    }

    public class State implements AllocationTracker.State<E> {
        @Override
        public void changeSize(int change) throws E {

        }

        @Override
        public void run() {

        }
    }
}
