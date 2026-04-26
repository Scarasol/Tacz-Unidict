package com.scarasol.tud.data;


import com.scarasol.tud.TudMod;
import com.scarasol.tud.api.data.SearchableModData;
import com.scarasol.tud.api.serialization.JsonData;
import com.scarasol.tud.api.serialization.JsonTypeId;
import com.scarasol.tud.manager.AmmoManager;
import com.scarasol.tud.util.data.DataManager;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Scarasol
 */
@JsonTypeId("tacz_unidict_gun_data")
public record GunData(ResourceLocation gunId, List<MagData> availableAmmo) implements JsonData, SearchableModData {

    @Override
    public String getId() {
        return gunId.toString();
    }

    @Override
    public void onLoaded() {
        DataManager.registerModData(this);
    }

    @Override
    public Path getPath() {
        return FMLPaths.CONFIGDIR.get().resolve(TudMod.MODID).resolve("gun_data").resolve(gunId().toString().replaceAll(":", "_") + ".json");
    }

    @Nullable
    public MagData getCurrentMag(ItemStack itemStack) {
        if (availableAmmo == null || availableAmmo.isEmpty()) {
            return null;
        }
        CompoundTag tag = itemStack.getTag();
        if (tag != null && tag.contains("TudCurrentAmmo")) {
            int currentAmmo = tag.getInt("TudCurrentAmmo");
            if (currentAmmo < availableAmmo().size()) {
                return availableAmmo.get(currentAmmo);
            }
        }
        return availableAmmo.get(0);
    }

    @Nullable
    public ResourceLocation getCurrentAmmo(ItemStack itemStack) {
        MagData magData = getCurrentMag(itemStack);
        if (magData == null) {
            return null;
        }
        return magData.ammoId();
    }

    public List<ItemStack> getAllAmmo() {
        List<ItemStack> result = new ArrayList<>();
        availableAmmo()
                .stream()
                .map(MagData::ammoId)
                .map(AmmoManager::getAmmoData)
                .filter(Objects::nonNull)
                .map(AmmoData::getAmmo)
                .filter(Objects::nonNull)
                .map(AmmoManager::getAmmoItemStack)
                .forEach(result::add);
        return result;
    }


    public boolean contains(ResourceLocation ammoId) {
        return availableAmmo()
                .stream()
                .map(MagData::ammoId)
                .anyMatch(ammoId::equals);
    }
}
