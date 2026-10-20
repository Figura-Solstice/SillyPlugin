package dev.celestial.silly.luaj.memory;

import dev.celestial.silly.SillySettings;
import dev.celestial.silly.SillyUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Cleaner;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

// https://github.com/FiguraMC/memory-tracker
public class AllocationTrackerImpl<E extends Throwable> implements AllocationTracker<E> {
    private static final Cleaner ALLOC_CLEANER = Cleaner.create();

    private final AtomicLong totalAllocated;
    private final long maxAllocation;
    private final long gracePeriodNanos;
    private final long trueMaxAllocation;
    private long gracePeriodBegan;
    private final Function<AllocationTracker.ErrorReason, E> errorFactory;
    Map<String, Integer> alive = new ConcurrentHashMap<>();

    @Override
    public long getTotal() {
        return totalAllocated.get();
    }

    public AllocationTrackerImpl(int maxAllocation, long gracePeriodNanos, int gracePeriodExtraSpace, Function<AllocationTracker.ErrorReason, E> errorFactory) {
        this.totalAllocated = new AtomicLong(0);
        this.maxAllocation = maxAllocation;
        this.gracePeriodNanos = gracePeriodNanos;
        this.trueMaxAllocation = (long)maxAllocation + (long)gracePeriodExtraSpace;
        this.gracePeriodBegan = -1;
        this.errorFactory = errorFactory;
    }

    @Override
    public void track(byte @NotNull [] byteArray) throws E {
        track(byteArray, OBJECT_SIZE + byteArray.length * BYTE_SIZE);
    }

    @Override
    public void track(int @NotNull [] intArray) throws E {
        track(intArray, OBJECT_SIZE + intArray.length * INT_SIZE);
    }

    @Override
    public void track(float @NotNull [] floatArray) throws E {
        track(floatArray, OBJECT_SIZE + floatArray.length * FLOAT_SIZE);
    }

    @Override
    public void track(Object @NotNull [] objectArray) throws E {
        track(objectArray, OBJECT_SIZE + objectArray.length * REFERENCE_SIZE);
    }

    @Override
    public void track(@NotNull String string) throws E {
        // TODO: ask limits what the FUCK this is
        track(string, OBJECT_SIZE + REFERENCE_SIZE + BYTE_SIZE + INT_SIZE + BOOLEAN_SIZE + string.length() * CHAR_SIZE);
    }

    @Override
    public Map<String, Integer> findCulprits() {
        return Collections.unmodifiableMap(alive);
    }

    @Override
    public AllocationTracker.State<E> track(@NotNull Object obj, int amount) throws E {
        var name = obj.getClass().getName();
        AllocationTracker.State<E> state = new State(amount, name);
        incrementMemory(amount);
        ALLOC_CLEANER.register(obj, state);
        alive.compute(name, (s, integer) -> {
            if (integer != null) return integer + 1;
            return 1;
        });
        return state;
    }

    private void incrementMemory(int amount) throws E {
        long newAmountAllocated = totalAllocated.getAndAdd(amount);
        if (newAmountAllocated > maxAllocation) {
            if (newAmountAllocated > trueMaxAllocation) {
                throw errorFactory.apply(ErrorReason.OVER_GRACE_MAXIMUM);
            } else if (gracePeriodBegan == -1) {
                if (SillySettings.MEMORY_GC.getBool())
                    System.gc();
                gracePeriodBegan = System.nanoTime();
            } else if (System.nanoTime() - gracePeriodBegan >= gracePeriodNanos) {
                throw errorFactory.apply(ErrorReason.GRACE_EXPIRED);
            }
        } else {
            gracePeriodBegan = -1;
        }
    }


    public class State implements AllocationTracker.State<E> {
        private int memoryReserved;
        private final String name;

        public State(int memoryReserved, String name) {
            this.memoryReserved = memoryReserved;
            this.name = name;
        }

        @Override
        public void changeSize(int change) throws E {
            if (change > 0) {
                memoryReserved += change;
                AllocationTrackerImpl.this.incrementMemory(change);
            } else if (change < 0) {
                memoryReserved += change;
                AllocationTrackerImpl.this.totalAllocated.getAndAdd(change);
            }
        }

        @Override
        public void run() {
            AllocationTrackerImpl.this.totalAllocated.getAndAdd(-memoryReserved);
            alive.computeIfPresent(name, (s, integer) -> {
                var n = integer - 1;
                if (n == 0) return null;
                return n;
            });
        }
    }
}
