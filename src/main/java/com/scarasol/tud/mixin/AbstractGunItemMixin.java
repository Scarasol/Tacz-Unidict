package com.scarasol.tud.mixin;


import com.scarasol.tud.manager.AmmoManager;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAnimationItem;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.inventory.tooltip.GunTooltip;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

/**
 * @author Scarasol
 */
@Mixin(AbstractGunItem.class)
public abstract class AbstractGunItemMixin extends Item implements IGun, IAnimationItem {

    public AbstractGunItemMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "getTooltipImage", cancellable = true, at = @At(value = "INVOKE", target = "Lcom/tacz/guns/resource/pojo/data/gun/GunData;getAmmoId()Lnet/minecraft/resources/ResourceLocation;"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void tud$getAmmoId(ItemStack stack, CallbackInfoReturnable<Optional<TooltipComponent>> cir, IGun iGun, Optional optional, CommonGunIndex gunIndex) {
        if (AmmoManager.canUseGeneralAmmo(getGunId(stack).toString(), gunIndex.getGunData().getAmmoId().toString())) {
            ResourceLocation ammo = AmmoManager.getAmmo(gunIndex.getGunData().getReloadData().getType().name().toLowerCase());
            if (ammo == null) {
                ammo = AmmoManager.getAmmo(gunIndex.getType());
            }
            if (ammo != null) {
                cir.setReturnValue(Optional.of(new GunTooltip(stack, iGun, ammo, gunIndex)));
                cir.cancel();
            }
        }
    }



}
