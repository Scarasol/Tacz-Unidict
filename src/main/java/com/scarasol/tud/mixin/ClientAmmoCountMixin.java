package com.scarasol.tud.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.scarasol.tud.manager.AmmoManager;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.gameplay.ClientAmmoCount;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Scarasol
 */
@Mixin(ClientAmmoCount.class)
public abstract class ClientAmmoCountMixin {

    @Inject(method = "get", remap = false, at = @At(
            value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;",
            ordinal = 0, remap = true
    ))
    private static void tud$countGeneralAmmo(LocalPlayer player, ItemStack gun, IGun iGun,
                                             CallbackInfoReturnable<Integer> cir,
                                             @Local(ordinal = 0) LocalIntRef count,
                                             @Local(ordinal = 1) ItemStack inventoryItem) {
        Item item = inventoryItem.getItem();
        if (item instanceof IAmmo || item instanceof IAmmoBox) {
            return;
        }
        if (AmmoManager.isAmmoOfGunItem(gun, inventoryItem)) {
            int current = count.get();
            count.set(current + Math.min(inventoryItem.getCount(), ClientAmmoCount.MAX_DISPLAY_COUNT - current));
        }
    }
}
