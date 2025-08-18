package com.scarasol.tud.mixin;

import com.scarasol.tud.manager.AmmoManager;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * @author Scarasol
 */
@Mixin(ModernKineticGunScriptAPI.class)
public abstract class ModernKineticGunScriptAPIMixin {

    @Shadow private ItemStack itemStack;

    @Inject(method = "lambda$hasAmmoToConsume$5", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;", ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void tud$hasAmmoToConsume(IItemHandler cap, CallbackInfoReturnable<Boolean> cir, int i, ItemStack checkAmmoStack) {
        Item ammo = checkAmmoStack.getItem();
        if (ammo instanceof IAmmo || ammo instanceof IAmmoBox) {
            return;
        }
        if (AmmoManager.isAmmoOfGunItem(itemStack, checkAmmoStack)) {
            cir.setReturnValue(true);
        }
    }
}
