package com.scarasol.tud.manager;

import com.google.common.collect.Maps;
import com.scarasol.tud.configuration.CommonConfig;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.resource.index.CommonGunIndex;
import net.minecraft.resources.ResourceLocation;
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
    private static boolean INIT;

    @Nullable
    public static Tuple<ResourceLocation, Boolean> getAmmo(String type) {
        if (!INIT) {
            init();
        }
        return TYPE_AMMO.get(type);
    }

    public static void init() {
        CommonConfig.TYPE_TO_AMMO.get()
                .forEach(string -> {
                    String[] info =string.split(",");
                    if (info.length >= 2) {
                        String ammo = info[1].trim();
                        boolean flag = ammo.startsWith("$");
                        if (flag) {
                            ammo = ammo.substring(1);
                        }
                        TYPE_AMMO.put(info[0].trim(), new Tuple<>(new ResourceLocation(ammo), flag));
                    }
                });
        INIT = true;
    }

    public static boolean canUseGeneralAmmo(String gunId, String ammoId) {
        return !CommonConfig.GUN_WHITELIST.get().contains(gunId) && !CommonConfig.AMMO_WHITELIST.get().contains(ammoId);
    }

    public static boolean isAmmoOfGunItem(ItemStack gun, ItemStack ammo) {
        Item var5 = gun.getItem();
        if (var5 instanceof IGun) {
            IGun iGun = (IGun)var5;
            ResourceLocation gunId = iGun.getGunId(gun);
            Optional<CommonGunIndex> commonGunIndex = TimelessAPI.getCommonGunIndex(gunId);
            String ammoIdStr = commonGunIndex.map((gunIndex) ->
                    gunIndex.getGunData().getAmmoId().toString()).orElse("");
            if (AmmoManager.canUseGeneralAmmo(gunId.toString(), ammoIdStr)) {
                return commonGunIndex.map((gunIndex) -> {
                    Tuple<ResourceLocation, Boolean> location = AmmoManager.getAmmo(gunIndex.getGunData().getReloadData().getType().name().toLowerCase());
                    if (location == null) {
                        location = AmmoManager.getAmmo(gunIndex.getType());
                    }
                    return location != null && location.getA().equals(ForgeRegistries.ITEMS.getKey(ammo.getItem()));
                }).orElse(false);
            }

        }

        return false;
    }
}
