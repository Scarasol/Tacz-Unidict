package com.scarasol.tud.mixin;

import com.scarasol.tud.data.AmmoData;
import com.scarasol.tud.data.GunData;
import com.scarasol.tud.data.MagData;
import com.scarasol.tud.manager.AmmoManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.tacz.guns.client.gameplay.LocalPlayerShoot")
public class LocalPlayerShootMixin {

    @Inject(method = "useSilenceSound", at = @At("HEAD"), cancellable = true, remap = false)
    private void overrideSilenceSound(CallbackInfoReturnable<Boolean> cir) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        ItemStack mainHand = player.getMainHandItem();
        GunData gunData = AmmoManager.getGunData(mainHand);
        if (gunData != null) {
            MagData magData = gunData.getCurrentMag(mainHand);
            if (magData != null && !magData.allowSuppress()) {
                cir.setReturnValue(false);
                return;
            }
        }

        AmmoData ammoData = AmmoManager.getCurrentAmmoData(mainHand);
        if (ammoData != null && ammoData.getSuppressSound() != null) {
            cir.setReturnValue(ammoData.getSuppressSound());
        }
    }
}