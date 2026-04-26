package com.scarasol.tud.data;

import com.scarasol.tud.TudMod;
import com.scarasol.tud.api.data.SearchableModData;
import com.scarasol.tud.api.functional.ModifierGetter;
import com.scarasol.tud.api.serialization.JsonData;
import com.scarasol.tud.api.serialization.JsonTypeId;
import com.scarasol.tud.util.data.DataManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.Map;

@JsonTypeId("tacz_unidict_modifier_data")
public record ModifierData(ResourceLocation modifierId, Map<String, Double> modifierMap) implements JsonData, SearchableModData {
    @Override
    public String getId() {
        return modifierId.toString();
    }

    @Override
    public void onLoaded() {
        DataManager.registerModData(this);
    }

    @Override
    public Path getPath() {
        return FMLPaths.CONFIGDIR.get().resolve(TudMod.MODID).resolve("modifier_data").resolve(modifierId.toString().replaceAll(":", "_") + ".json");
    }

    public double getModifier(Entity entity) {
        return DataManager.getModDataRegisterData(ModifierGetter.class)
                .stream()
                .map(modifierGetter -> modifierGetter.getModifier(entity, modifierMap))
                .filter(aDouble -> aDouble >= 0)
                .findFirst().orElse(1.0);
    }
}
