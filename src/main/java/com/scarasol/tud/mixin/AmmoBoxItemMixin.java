package com.scarasol.tud.mixin;

import com.scarasol.tud.manager.AmmoManager;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.nbt.AmmoBoxItemDataAccessor;
import com.tacz.guns.item.AmmoBoxItem;
import com.tacz.guns.resource.index.CommonGunIndex;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

/**
 * @author Scarasol
 */
@Mixin(AmmoBoxItem.class)
public abstract class AmmoBoxItemMixin extends Item implements DyeableLeatherItem, AmmoBoxItemDataAccessor {

    public AmmoBoxItemMixin(Properties properties) {
        super(properties);
    }

    @Override
    @Unique
    public boolean isAmmoBoxOfGun(ItemStack gun, ItemStack ammoBox) {
        Item var5 = gun.getItem();
        if (var5 instanceof IGun) {
            IGun iGun = (IGun)var5;
            var5 = ammoBox.getItem();
            if (var5 instanceof IAmmoBox) {
                IAmmoBox iAmmoBox = (IAmmoBox)var5;
                if (this.isAllTypeCreative(ammoBox)) {
                    return true;
                }

                ResourceLocation gunId = iGun.getGunId(gun);
                ResourceLocation ammoId = iAmmoBox.getAmmoId(ammoBox);
                if (ammoId.equals(DefaultAssets.EMPTY_AMMO_ID)) {
                    return false;
                }
                Optional<CommonGunIndex> commonGunIndex = TimelessAPI.getCommonGunIndex(gunId);

                String ammoIdStr = commonGunIndex.map((gunIndex) ->
                        gunIndex.getGunData().getAmmoId().toString()).orElse("");
                if (AmmoManager.canUseGeneralAmmo(gunId.toString(), ammoIdStr)) {
                    Tuple<ResourceLocation, Boolean> location = AmmoManager.getAmmo(gun);
                    return location != null && ammoId.equals(location.getA());
                }
                return ammoIdStr.equals(ammoId.toString());
            }
        }

        return false;
    }
}
