package com.scarasol.tud.inventory.tooltip;

import com.tacz.guns.api.item.IGun;
import com.tacz.guns.inventory.tooltip.GunTooltip;
import com.tacz.guns.resource.index.CommonGunIndex;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * @author Scarasol
 */
public class CustomGunTooltip extends GunTooltip {

    private final boolean flag;

    public CustomGunTooltip(ItemStack gun, IGun iGun, ResourceLocation ammoId, CommonGunIndex gunIndex, boolean flag) {
        super(gun, iGun, ammoId, gunIndex);
        this.flag = flag;
    }

    public boolean isFlag() {
        return flag;
    }
}
