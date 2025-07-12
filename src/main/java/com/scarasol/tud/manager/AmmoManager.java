package com.scarasol.tud.manager;

import com.google.common.collect.Maps;
import com.scarasol.tud.configuration.CommonConfig;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * @author Scarasol
 */
public class AmmoManager {
    private static final Map<String, ResourceLocation> TYPE_AMMO = Maps.newHashMap();
    private static boolean INIT;

    @Nullable
    public static ResourceLocation getAmmo(String type) {
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
                        TYPE_AMMO.put(info[0].trim(), new ResourceLocation(info[1].trim()));
                    }
                });
        INIT = true;
    }

    public static boolean canUseGeneralAmmo(String gunId, String ammoId) {
        return !CommonConfig.GUN_WHITELIST.get().contains(gunId) && !CommonConfig.AMMO_WHITELIST.get().contains(ammoId);
    }
}
