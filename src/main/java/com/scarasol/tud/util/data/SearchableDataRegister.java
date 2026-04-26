package com.scarasol.tud.util.data;

import com.scarasol.tud.api.data.SearchableModData;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Scarasol
 */
public class SearchableDataRegister<T extends SearchableModData> extends DataRegister<T> {

    public Map<String, T> dataIndex = new HashMap<>();

    public SearchableDataRegister(int index, boolean fifo) {
        super(index, fifo);
    }

    @Override
    public void register(T value) {
        super.register(value);
        dataIndex.put(value.getId(), value);
    }


    public boolean contains(T value) {
        return dataIndex.containsKey(value.getId());
    }

    public T getValueById(String id) {
        return dataIndex.get(id);
    }


    @Override
    public void clear() {
        super.clear();
        dataIndex.clear();
    }

}
