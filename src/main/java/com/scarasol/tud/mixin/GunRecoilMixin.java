package com.scarasol.tud.mixin;

import com.scarasol.tud.data.AmmoData;
import com.scarasol.tud.data.MagData;
import com.scarasol.tud.manager.AmmoManager;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.resource.pojo.data.gun.GunRecoil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GunRecoil.class)
public class GunRecoilMixin {

    @ModifyVariable(method = "genPitchSplineFunction", at = @At("HEAD"), argsOnly = true, remap = false)
    private float modifyPitchModifier(float modifier) {
        return applyRecoilMultiplier(modifier, true);
    }

    @ModifyVariable(method = "genYawSplineFunction", at = @At("HEAD"), argsOnly = true, remap = false)
    private float modifyYawModifier(float modifier) {
        return applyRecoilMultiplier(modifier, false);
    }

    private float applyRecoilMultiplier(float originalModifier, boolean isPitch) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return originalModifier;
        ItemStack gunItem = player.getMainHandItem();
        if (!(gunItem.getItem() instanceof IGun)) return originalModifier;

        AmmoData ammoData = AmmoManager.getCurrentAmmoData(gunItem);
        MagData magData = null;
        com.scarasol.tud.data.GunData gunData = AmmoManager.getGunData(gunItem);
        if (gunData != null) magData = gunData.getCurrentMag(gunItem);

        float multiplier = 1.0f;
        if (ammoData != null) {
            if (isPitch) {
                if (ammoData.getVerticalRecoilModifier() != null) multiplier *= ammoData.getVerticalRecoilModifier();
            } else {
                if (ammoData.getHorizontalRecoilModifier() != null) multiplier *= ammoData.getHorizontalRecoilModifier();
            }
        }
        if (magData != null) {
            if (isPitch) {
                multiplier *= magData.getVerticalRecoilModifier();
            } else {
                multiplier *= magData.getHorizontalRecoilModifier();
            }
        }

        if (Math.abs(multiplier - 1.0f) > 0.0001f) {
            return originalModifier * multiplier;
        }
        return originalModifier;
    }
}