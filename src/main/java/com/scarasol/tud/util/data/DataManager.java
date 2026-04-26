package com.scarasol.tud.util.data;

import com.scarasol.tud.api.data.ModData;
import com.scarasol.tud.api.data.SearchableModData;
import com.scarasol.tud.util.io.JsonTypeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

/**
 * @author Scarasol
 */
public class DataManager {
    private static final Map<Class<? extends ModData>, DataRegister<?>> DATA_REGISTER_MAP = new HashMap<>();

    public static <T extends ModData> void registerType(Class<T> type, int index, boolean fifo) {
        DATA_REGISTER_MAP.put(type, new DataRegister<T>(index, fifo));
    }

    public static <T extends SearchableModData> void registerSearchableType(Class<T> type, int index, boolean fifo) {
        DATA_REGISTER_MAP.put(type, new SearchableDataRegister<>(index, fifo));
    }

    public static <T extends ModData> void registerModData(T modData) {
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) modData.getClass();
        registerModData(type, modData);
    }

    public static <T extends ModData> void registerModData(Class<T> type, T modData) {
        @SuppressWarnings("unchecked")
        DataRegister<T> register = (DataRegister<T>) DATA_REGISTER_MAP.get(type);
        if (register != null) {
            register.register(modData);
        }
    }

    @Nullable
    public static <T extends SearchableModData> T getSearchableModData(Class<T> type, @NotNull String searchedId) {
        DataRegister<?> raw = DATA_REGISTER_MAP.get(type);
        if (!(raw instanceof SearchableDataRegister<?> searchableRaw)) {
            return null;
        }

        @SuppressWarnings("unchecked")
        SearchableDataRegister<T> searchable = (SearchableDataRegister<T>) searchableRaw;

        return searchable.getValueById(searchedId);
    }

    public static <T extends ModData> List<T> getModDataRegisterData(Class<T> type) {
        @SuppressWarnings("unchecked")
        DataRegister<T> register = (DataRegister<T>) DATA_REGISTER_MAP.get(type);

        if (register != null) {
            return register.stream().toList();
        }
        return new ArrayList<>();
    }

    public static <T extends ModData> void clear(Class<T> type) {
        @SuppressWarnings("unchecked")
        DataRegister<T> register = (DataRegister<T>) DATA_REGISTER_MAP.get(type);
        if (register != null) {
            register.clear();
        }
    }

    public static void clearAll() {
        DATA_REGISTER_MAP.forEach((key, value) -> value.clear());
    }


}
