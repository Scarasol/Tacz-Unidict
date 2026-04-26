package com.scarasol.tud.util.io;

import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.scarasol.tud.api.serialization.JsonData;

import java.io.IOException;

/**
 * @author Scarasol
 */
public record PolymorphicJsonDataAdapterFactory(JsonTypeRegistry registry, String typeField) implements TypeAdapterFactory {

    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {

        if (!JsonData.class.isAssignableFrom(type.getRawType())) {
            return null;
        }

        return (TypeAdapter<T>) new TypeAdapter<JsonData>() {

            @Override
            public void write(JsonWriter out, JsonData value) throws IOException {
                if (value == null) {
                    out.nullValue();
                    return;
                }

                String id = value.typeId();
                if (id == null) {
                    throw new JsonParseException("Unregistered JsonData class: " + value.getClass().getName());
                }


                TypeAdapter<JsonData> delegate = gson.getDelegateAdapter(
                        PolymorphicJsonDataAdapterFactory.this,
                        (TypeToken<JsonData>) TypeToken.get((Class<? extends JsonData>) value.getClass())
                );

                JsonElement tree = delegate.toJsonTree(value);
                if (!tree.isJsonObject()) {
                    throw new JsonParseException("JsonData must serialize to JsonObject: " + value.getClass().getName());
                }

                JsonObject obj = tree.getAsJsonObject();
                obj.addProperty(typeField, id);

                Streams.write(obj, out);
            }

            @Override
            public JsonData read(JsonReader in) {
                JsonElement tree = Streams.parse(in);
                if (tree == null || tree.isJsonNull()) {
                    return null;
                }
                if (!tree.isJsonObject()) {

                    throw new JsonParseException("Expected JsonObject for JsonData");
                }

                JsonObject obj = tree.getAsJsonObject();
                JsonElement typeElem = obj.get(typeField);
                if (typeElem == null || !typeElem.isJsonPrimitive()) {

                    throw new JsonParseException("Missing '" + typeField + "' discriminator");
                }

                String id = typeElem.getAsString();
                Class<? extends JsonData> clazz = registry.classOf(id);
                if (clazz == null) {
                    throw new JsonParseException("Unknown '" + typeField + "': " + id);
                }

                JsonObject clone = obj.deepCopy();
                clone.remove(typeField);

                TypeAdapter<? extends JsonData> delegate = gson.getDelegateAdapter(
                        PolymorphicJsonDataAdapterFactory.this,
                        TypeToken.get(clazz)
                );
                return delegate.fromJsonTree(clone);
            }
        };
    }
}
