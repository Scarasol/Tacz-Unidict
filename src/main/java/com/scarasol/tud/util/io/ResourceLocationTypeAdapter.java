package com.scarasol.tud.util.io;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;


/**
 * ResourceLocation的序列化
 * @author Scarasol
 */
public class ResourceLocationTypeAdapter extends TypeAdapter<ResourceLocation> {

    @Override
    public void write(JsonWriter out, ResourceLocation value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(value.toString());
    }

    @Override
    public ResourceLocation read(JsonReader in) throws IOException {
        JsonToken token = in.peek();
        if (token == JsonToken.NULL) {
            in.nextNull();
            return null;
        }


        String s = in.nextString();
        if (s == null) {
            return null;
        }
        s = s.trim();
        if (s.isEmpty() || s.equalsIgnoreCase("null")) {
            return null;
        }
        ResourceLocation rl = ResourceLocation.tryParse(s);
        if (rl == null) {
            throw new JsonParseException("Invalid ResourceLocation: " + s);
        }
        return rl;
    }
}
