package com.scarasol.tud.util.io;

import com.scarasol.tud.api.serialization.JsonTypeId;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * JsonTypeId注解的缓存。
 * @author Scarasol
 */
public final class JsonTypeIds {
    private JsonTypeIds() {}

    private static final ClassValue<String> CACHE = new ClassValue<>() {
        @Override
        protected String computeValue(Class<?> type) {
            JsonTypeId ann = type.getAnnotation(JsonTypeId.class);
            if (ann == null) {
                return null;
            }
            String v = ann.value();
            return v.isBlank() ? null : v;
        }
    };

    @Nullable
    public static String idOf(Class<?> clazz) {
        Objects.requireNonNull(clazz, "clazz");
        return CACHE.get(clazz);
    }

    public static String requireIdOf(Class<?> clazz) {
        String id = idOf(clazz);
        if (id == null) {
            throw new IllegalStateException("Missing or blank @JsonTypeId on " + clazz.getName());
        }
        return id;
    }
}
