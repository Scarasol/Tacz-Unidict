package com.scarasol.tud.util.io;

import com.google.gson.*;
import com.scarasol.tud.TudMod;
import com.scarasol.tud.api.serialization.JsonData;
import com.scarasol.tud.init.TudModData;
import com.scarasol.tud.util.data.DataManager;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Locale;
import java.util.Objects;

/**
 * 进行序列化/反序列化操作的类
 *
 * @author Scarasol
 */
public class ModGson {
    public static final String TYPE_FIELD = "mod_data_type";

    private final JsonTypeRegistry registry;
    private final Gson gson;

    public static final ModGson INSTANCE = new ModGson(new JsonTypeRegistry());


    public ModGson(JsonTypeRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.gson = createGson(this.registry);
    }

    public <T extends JsonData> void register(Class<T> clazz) {
        registry.register(clazz);
    }

    public Gson gson() {
        return gson;
    }

    public static Gson createGson(JsonTypeRegistry registry) {
        return new GsonBuilder()
                .registerTypeAdapter(ResourceLocation.class, new ResourceLocationTypeAdapter())
                .registerTypeAdapterFactory(new PolymorphicJsonDataAdapterFactory(registry, TYPE_FIELD))
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
    }


    public void write(@NotNull Path path, @NotNull JsonData value) throws IOException {

        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        String json = gson.toJson(value, JsonData.class);

        Path tmp = path.resolveSibling(path.getFileName() + ".tmp");
        Files.writeString(tmp, json, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);

        try {
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }


    public boolean read(@NotNull Path path) throws IOException {
        if (Files.notExists(path) || !Files.isRegularFile(path)) {
            return false;
        }

        String json = Files.readString(path, StandardCharsets.UTF_8);
        if (json.isBlank()) {
            return false;
        }

        JsonElement elem;
        try {
            elem = JsonParser.parseString(json);
        } catch (Exception e) {
            TudMod.LOGGER.warn("[JsonIO] skip invalid json: " + path);
            return false;
        }

        if (!elem.isJsonObject()) {
            return false;
        }

        JsonObject obj = elem.getAsJsonObject();

        String type = extractType(obj);
        if (type == null) {
            return false;
        }
        if (registry.classOf(type) == null) {
            return false;
        }

        try {
            JsonData data = gson.fromJson(obj, JsonData.class);
            if (data == null) {
                return false;
            }

            data.onLoaded();
            return true;
        } catch (Exception e) {
            TudMod.LOGGER.warn("[JsonIO] skip parse fail: " + path + " (" + e.getMessage() + ")");
            return false;
        }
    }

    public void loadAll(@NotNull Path root) throws IOException {
        if (Files.exists(root) && !Files.isDirectory(root)) {
            return;
        }
        if (Files.notExists(root)) {
            init(root);
        }
        try (var stream = Files.walk(root)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase(Locale.ROOT).endsWith(".json"))
                    .forEach(p -> {
                        try {
                            read(p);
                        } catch (IOException e) {
                            TudMod.LOGGER.warn("[JsonIO] skip io fail: " + p + " (" + e.getMessage() + ")");
                        }
                    });
        }
    }

    private static String extractType(JsonObject obj) {
        JsonElement typeElem = obj.get(TYPE_FIELD);
        if (typeElem == null || !typeElem.isJsonPrimitive()) {
            return null;
        }
        String s = typeElem.getAsString();
        return (s == null || s.isBlank()) ? null : s;
    }

    private void init(Path root) throws IOException {
        TudModData.initModData(root);
    }

}
