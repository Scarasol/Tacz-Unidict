package com.scarasol.tud.mixin;

import com.scarasol.tud.manager.AmmoManager;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.client.gui.overlay.GunHudOverlay;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * @author Scarasol
 */
@Mixin(GunHudOverlay.class)
public abstract class GunHudOverlayMixin implements IGuiOverlay {

    @Shadow private static int cacheInventoryAmmoCount;

    @Inject(method = "handleInventoryAmmo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;", ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void tud$handleInventoryAmmo(ItemStack stack, Inventory inventory, CallbackInfo ci, int i, ItemStack inventoryItem) {
        Item item = inventoryItem.getItem();
        if (item instanceof IAmmo || item instanceof IAmmoBox) {
            return;
        }
        if (AmmoManager.isAmmoOfGunItem(stack, inventoryItem)) {
            cacheInventoryAmmoCount += inventoryItem.getCount();
        }
    }
}
