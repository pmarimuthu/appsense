package com.maybank.appsense.core.buffer;

import com.maybank.appsense.core.config.AppConstant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/** Fixed-capacity buffer that drops its oldest element when full. Not thread-safe. */
public final class RingBuffer<T> {

    private final Object[] slots;
    private int head;
    private int size;
    private long droppedCount;

    public RingBuffer(int capacity) {
        this.slots = new Object[Math.max(capacity, 1)];
    }

    /** Appends an item and returns true when the oldest item was dropped to make room. */
    public boolean add(T item) {
        if (size == slots.length) {
            slots[head] = item;
            head = (head + 1) % slots.length;
            droppedCount++;
            return true;
        }
        slots[(head + size) % slots.length] = item;
        size++;
        return false;
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        Objects.checkIndex(index, size);
        return (T) slots[(head + index) % slots.length];
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return slots.length;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public long getDroppedCount() {
        return droppedCount;
    }

    public int lastIndexOf(Predicate<T> match) {
        for (int index = size - 1; index >= 0; index--) {
            if (match.test(get(index))) {
                return index;
            }
        }
        return AppConstant.NOT_FOUND;
    }

    public void dropFirst(int count) {
        int removed = Math.min(count, size);
        for (int index = 0; index < removed; index++) {
            slots[(head + index) % slots.length] = null;
        }
        head = (head + removed) % slots.length;
        size -= removed;
    }

    public void clear() {
        dropFirst(size);
        head = 0;
        droppedCount = 0;
    }

    public List<T> toList() {
        List<T> copy = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            copy.add(get(index));
        }
        return copy;
    }
}
