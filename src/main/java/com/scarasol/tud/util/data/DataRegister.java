package com.scarasol.tud.util.data;

import com.scarasol.tud.api.data.ModData;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED),
                false
        );
    }

    public void clear() {
        deque.clear();
    }
}
