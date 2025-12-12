package com.scarasol.tud.manager;

import com.google.common.collect.Maps;
import com.scarasol.tud.configuration.CommonConfig;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.resource.index.CommonGunIndex;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Scarasol
 */
public class AmmoManager {
    private static final Map<String, Tuple<ResourceLocation, Boolean>> TYPE_AMMO = Maps.newHashMap();
    private static final Map<TagKey<Item>, Tuple<ResourceLocation, Boolean>> TYPE_AMMO_TAG = Maps.newHashMap();
    private static boolean INIT;

    @Nullable
    public static Tuple<ResourceLocation, Boolean> getAmmo(ItemStack itemStack) {
        if (!INIT) {
            init();
        }
        IGun iGun = IGun.getIGunOrNull(itemStack);
        if (iGun == null) {
            return null;
        }
        for (Map.Entry<TagKey<Item>, Tuple<ResourceLocation, Boolean>> entry : TYPE_AMMO_TAG.entrySet()) {
            if (itemStack.is(entry.getKey())) {
                return entry.getValue();
            }
        }
        ResourceLocation gunId = iGun.getGunId(itemStack);
        Optional<CommonGunIndex> commonGunIndex = TimelessAPI.getCommonGunIndex(gunId);
        if (commonGunIndex.isPresent()) {
            CommonGunIndex gunIndex = commonGunIndex.get();
            Tuple<ResourceLocation, Boolean> result = TYPE_AMMO.get(gunIndex.getGunData().getReloadData().getType().name().toLowerCase());
            if (result == null) {
               result = TYPE_AMMO.get(gunIndex.getType());
            }
            return result;

        }
        return null;
    }

    public static void init() {
        CommonConfig.TYPE_TO_AMMO.get()
                .forEach(string -> {
                    String[] info =string.split(",");
                    if (info.length >= 2) {
                        String gun = info[0].trim();
                        boolean tag = gun.startsWith("#");
                        String ammo = info[1].trim();
                        boolean flag = ammo.startsWith("$");
                        if (flag) {
                            ammo = ammo.substring(1);
                        }
                        if (tag) {
                            gun = gun.substring(1);
                            TYPE_AMMO_TAG.put(TagKey.create(Registries.ITEM, new ResourceLocation(gun)), new Tuple<>(new ResourceLocation(ammo), flag));
                        } else {
                            TYPE_AMMO.put(info[0].trim(), new Tuple<>(new ResourceLocation(ammo), flag));
                        }
                    }
                });
        INIT = true;
    }

    public static boolean canUseGeneralAmmo(String gunId, String ammoId) {
        return !CommonConfig.GUN_WHITELIST.get().contains(gunId) && !CommonConfig.AMMO_WHITELIST.get().contains(ammoId);
    }

    public static boolean isAmmoOfGunItem(ItemStack gun, ItemStack ammo) {
        Tuple<ResourceLocation, Boolean> location = AmmoManager.getAmmo(gun);
        return location != null && location.getA().equals(ForgeRegistries.ITEMS.getKey(ammo.getItem()));

    }

    public static ItemStack getGunAmmo(ItemStack gunItem) {
        Tuple<ResourceLocation, Boolean> location = AmmoManager.getAmmo(gunItem);
        if (location != null) {
            if (location.getB()) {
                Item item = ForgeRegistries.ITEMS.getValue(location.getA());
                if (item != null) {
                    return new ItemStack(item);
                }
            } else {
                return AmmoItemBuilder.create().setId(location.getA()).build();
            }
        }
        return ItemStack.EMPTY;
    }
}
