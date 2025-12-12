package com.scarasol.tud.mixin;

import com.scarasol.tud.manager.AmmoManager;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.nbt.AmmoItemDataAccessor;
import com.tacz.guns.item.AmmoItem;
import com.tacz.guns.resource.index.CommonGunIndex;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

/**
 * @author Scarasol
 */
@Mixin(AmmoItem.class)
public abstract class AmmoItemMixin extends Item implements AmmoItemDataAccessor {

    public AmmoItemMixin(Properties properties) {
        super(properties);
    }

    @Override
    @Unique
    public boolean isAmmoOfGun(ItemStack gun, ItemStack ammo) {
        Item var5 = gun.getItem();
        if (var5 instanceof IGun) {
            IGun iGun = (IGun)var5;
            var5 = ammo.getItem();
            if (var5 instanceof IAmmo) {
                IAmmo iAmmo = (IAmmo)var5;
                ResourceLocation gunId = iGun.getGunId(gun);
                ResourceLocation ammoId = iAmmo.getAmmoId(ammo);
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
