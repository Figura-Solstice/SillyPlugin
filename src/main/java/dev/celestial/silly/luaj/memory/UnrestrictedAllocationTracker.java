package dev.celestial.silly.luaj.memory;

import dev.celestial.silly.SillySettings;
import dev.celestial.silly.SillyUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Cleaner;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class UnrestrictedAllocationTracker<E extends Throwable> implements AllocationTracker<E> {
    private final AtomicLong totalAllocated;
    private final Map<String, Integer> alive;
    private final Cleaner ALLOC_CLEANER = Cleaner.create();
    @Override
    public long getTotal() {
        return totalAllocated.get();
    }

    public UnrestrictedAllocationTracker() {
        this.totalAllocated = new AtomicLong(0);
        alive = new ConcurrentHashMap<>();
    }

    @Override
    public void track(byte @NotNull [] byteArray)  {
        track(byteArray, OBJECT_SIZE + byteArray.length * BYTE_SIZE);
    }

    @Override
    public void track(int @NotNull [] intArray)  {
        track(intArray, OBJECT_SIZE + intArray.length * INT_SIZE);
    }

    @Override
    public void track(float @NotNull [] floatArray)  {
        track(floatArray, OBJECT_SIZE + floatArray.length * FLOAT_SIZE);
    }

    @Override
    public void track(Object @NotNull [] objectArray)  {
        track(objectArray, OBJECT_SIZE + objectArray.length * REFERENCE_SIZE);
    }

    @Override
    public void track(@NotNull String string)  {
        // TODO: ask limits what the FUCK this is
        track(string, OBJECT_SIZE + REFERENCE_SIZE + BYTE_SIZE + INT_SIZE + BOOLEAN_SIZE + string.length() * CHAR_SIZE);
    }

    @Override
    public Map<String, Integer> findCulprits() {
        return Collections.unmodifiableMap(alive);
//        return Map.of();
    }

    @Override
    public AllocationTracker.State<E> track(@NotNull Object obj, int amount)  {
        var name = obj.getClass().getName();
        AllocationTracker.State<E> state = new State<>(amount, name);
        totalAllocated.getAndAdd(amount);
        ALLOC_CLEANER.register(obj, state);
        alive.compute(name, (s, integer) -> {
            if (integer != null) return integer + 1;
            return 1;
        });
        return state;
    }

    public class State<E extends Throwable> implements AllocationTracker.State<E> {
        private int memoryReserved;
        private final String name;

        public State(int memoryReserved, String name) {
            this.memoryReserved = memoryReserved;
            this.name = name;
        }
        @Override
        public void changeSize(int change)  {
            UnrestrictedAllocationTracker.this.totalAllocated.getAndAdd(change);
            memoryReserved += change;
        }

        @Override
        public void run() {
            UnrestrictedAllocationTracker.this.totalAllocated.getAndAdd(-memoryReserved);
            alive.computeIfPresent(name, (s, integer) -> {
                var n = integer - 1;
                if (n == 0) return null;
                return n;
            });
        }
    }
}
