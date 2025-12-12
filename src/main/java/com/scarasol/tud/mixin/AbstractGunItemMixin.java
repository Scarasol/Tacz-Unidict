package com.scarasol.tud.mixin;


import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.scarasol.tud.TudMod;
import com.scarasol.tud.inventory.tooltip.CustomGunTooltip;
import com.scarasol.tud.manager.AmmoManager;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IAnimationItem;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.resource.index.CommonAmmoIndex;
import com.tacz.guns.resource.index.CommonGunIndex;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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

    @Unique
    private int tud$currentAmmo;

    @Inject(method = "getTooltipImage", cancellable = true, at = @At(value = "INVOKE", target = "Lcom/tacz/guns/resource/pojo/data/gun/GunData;getAmmoId()Lnet/minecraft/resources/ResourceLocation;"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void tud$getAmmoId(ItemStack stack, CallbackInfoReturnable<Optional<TooltipComponent>> cir, IGun iGun, Optional optional, CommonGunIndex gunIndex) {
        if (AmmoManager.canUseGeneralAmmo(getGunId(stack).toString(), gunIndex.getGunData().getAmmoId().toString())) {
            Tuple<ResourceLocation, Boolean> ammo = AmmoManager.getAmmo(stack);
            if (ammo != null) {
                cir.setReturnValue(Optional.of(new CustomGunTooltip(stack, iGun, ammo.getA(), gunIndex, ammo.getB())));
            }
        }
    }

    @Inject(method = "lambda$canReload$1", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;", ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void tud$checkItem(ItemStack gunItem, IItemHandler cap, CallbackInfoReturnable<Boolean> cir, int i, ItemStack checkAmmoStack) {
        Item item = checkAmmoStack.getItem();
        if (item instanceof IAmmo || item instanceof IAmmoBox) {
            return;
        }
        if (AmmoManager.isAmmoOfGunItem(gunItem, checkAmmoStack)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "findAndExtractInventoryAmmo", remap = false, at = @At("HEAD"))
    private void tud$findAndExtractInventoryAmmoItemHead(IItemHandler itemHandler, ItemStack gunItem, int needAmmoCount, CallbackInfoReturnable<Integer> cir) {
        tud$currentAmmo = needAmmoCount;
    }

    @Inject(method = "findAndExtractInventoryAmmo", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;", ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void tud$findAndExtractInventoryAmmoItem(IItemHandler itemHandler, ItemStack gunItem, int needAmmoCount, CallbackInfoReturnable<Integer> cir, int cnt, int i, ItemStack checkAmmoStack) {
        Item ammo = checkAmmoStack.getItem();
        if (ammo instanceof IAmmo || ammo instanceof IAmmoBox) {
            return;
        }
        if (AmmoManager.isAmmoOfGunItem(gunItem, checkAmmoStack)) {
            ItemStack extractItem = itemHandler.extractItem(i, cnt, false);
            tud$currentAmmo -= extractItem.getCount();
            if (tud$currentAmmo <= 0) {
                cir.setReturnValue(needAmmoCount - tud$currentAmmo);
            }
        }
    }

    @Inject(method = "findAndExtractInventoryAmmo", remap = false, cancellable = true, at = @At("TAIL"))
    private void tud$findAndExtractInventoryAmmoItemReturn(IItemHandler itemHandler, ItemStack gunItem, int needAmmoCount, CallbackInfoReturnable<Integer> cir) {
        if (tud$currentAmmo < needAmmoCount) {
            cir.setReturnValue(needAmmoCount - tud$currentAmmo);
        }
    }

    @Inject(method = "lambda$hasInventoryAmmo$6", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;", ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void tud$hasInventoryAmmoItem(ItemStack gun, IItemHandler cap, CallbackInfoReturnable<Boolean> cir, int i, ItemStack checkAmmoStack) {
        Item item = checkAmmoStack.getItem();
        if (item instanceof IAmmo || item instanceof IAmmoBox) {
            return;
        }
        if (AmmoManager.isAmmoOfGunItem(gun, checkAmmoStack)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "lambda$dropAllAmmo$2", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/items/ItemHandlerHelper;giveItemToPlayer(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void tud$dropAllAmmo(int ammoCount, ResourceLocation ammoId, Player player, ItemStack gunItem, CommonAmmoIndex ammoIndex, CallbackInfo ci, int stackSize, int tmpAmmoCount, int roundCount, int i, int count, ItemStack ammoItem) {
        ItemStack itemStack = AmmoManager.getGunAmmo(gunItem);
        if (!itemStack.isEmpty() && !ItemStack.isSameItemSameTags(itemStack, ammoItem)) {
            itemStack.setCount(count);
            ammoItem.setCount(0);
            ItemHandlerHelper.giveItemToPlayer(player, itemStack);
        }
    }


}
