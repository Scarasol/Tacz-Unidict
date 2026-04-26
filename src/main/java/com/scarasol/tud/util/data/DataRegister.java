package com.scarasol.tud.util.data;

import com.scarasol.tud.api.data.ModData;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * @author Scarasol
 */
public class DataRegister<T extends ModData> implements Iterable<T> {

    private final Deque<T> deque = new ArrayDeque<>();
    private final int index;
    private final boolean fifo;

    public DataRegister(int index, boolean fifo) {
        this.index = index;
        this.fifo = fifo;
    }

    @Override
    public Iterator<T> iterator() {
        return fifo ? deque.descendingIterator() : deque.iterator();
    }

    public void register(T value) {
        deque.push(value);
    }

    public int getIndex() {
        return index;
    }

    public Stream<T> stream() {
        return deque.stream();
    }

    public void clear() {
        deque.clear();
    }
}
