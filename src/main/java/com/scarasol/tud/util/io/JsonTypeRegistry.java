package com.scarasol.tud.util.io;


import com.scarasol.tud.api.serialization.JsonData;
import com.scarasol.tud.api.serialization.JsonTypeId;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * 注册序列化类
 * @author Scarasol
 */
public class JsonTypeRegistry {

    private final Map<String, Class<? extends JsonData>> idToClass = new HashMap<>();


    public <T extends JsonData> void register(@NotNull String id, @NotNull Class<T> clazz) {
        Class<? extends JsonData> oldClass = idToClass.putIfAbsent(id, clazz);
        if (oldClass != null && oldClass != clazz) {
            throw new IllegalStateException("Duplicate JsonTypeId '" + id + "': " + oldClass.getName() + " vs " + clazz.getName());
        }

    }


    public <T extends JsonData> void register(@NotNull Class<T> clazz) {

        JsonTypeId ann = clazz.getAnnotation(JsonTypeId.class);
        if (ann == null || ann.value().isBlank()) {
            throw new IllegalStateException("Missing @JsonTypeId on " + clazz.getName());
        }
        register(ann.value(), clazz);
    }

    public Class<? extends JsonData> classOf(String id) {
        return idToClass.get(id);
    }


}
