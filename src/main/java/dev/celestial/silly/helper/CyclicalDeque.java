package dev.celestial.silly.helper;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractCollection;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class CyclicalDeque<T> extends AbstractCollection<T> {
    private Deque<T> inner = new ArrayDeque<>();
    private final int capacity;

    public CyclicalDeque(int capacity) {
        this.capacity = capacity;
    }

    public CyclicalDeque() {
        this(16);
    }

    public void push(T obj) {
        if (inner.size() == capacity)
            inner.pollLast();
        inner.push(obj);
    }

    public T getFirst() {
        return inner.getFirst();
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return inner.iterator();
    }

    @Override
    public int size() {
        return inner.size();
    }
}
