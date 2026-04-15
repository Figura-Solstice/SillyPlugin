package dev.celestial.silly.helper;

import com.llamalad7.mixinextras.lib.apache.commons.tuple.ImmutablePair;
import com.llamalad7.mixinextras.lib.apache.commons.tuple.Pair;

import java.util.function.Consumer;

public class AutoProfile implements AutoCloseable {
    public long start;
    public Consumer<Pair<Long, Long>> callback;


    public AutoProfile(Consumer<Pair<Long, Long>> cb) {
        this.start = System.nanoTime();
        this.callback = cb;
    }

    public static AutoProfile start(Consumer<Pair<Long, Long>> cb) {
        return new AutoProfile(cb);
    }

    @Override
    public void close() {
        callback.accept(new ImmutablePair<>(start, System.nanoTime()));
    }
}
