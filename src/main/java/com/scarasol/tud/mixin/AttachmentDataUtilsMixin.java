package com.scarasol.tud.mixin;

import com.scarasol.tud.data.TaczGunDataMap;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.util.AttachmentDataUtils;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * @author Scarasol
 */
@Mixin(value = AttachmentDataUtils.class, remap = false)
public abstract class AttachmentDataUtilsMixin {

    @ModifyVariable(
            method = "getAmmoCountWithAttachment(Lnet/minecraft/world/item/ItemStack;Lcom/tacz/guns/resource/pojo/data/gun/GunData;)I",
            at = @At("HEAD"),
            argsOnly = true,
            index = 1,
            require = 0
    )
    private static GunData tud$replaceGunData$getAmmoCountWithAttachment(GunData originalGunData, ItemStack gunItem) {
        GunData custom = TaczGunDataMap.getCustomGunData(gunItem, originalGunData);
        return custom != null ? custom : originalGunData;
    }


    @ModifyVariable(
            method = "isExplodeEnabled(Lnet/minecraft/world/item/ItemStack;Lcom/tacz/guns/resource/pojo/data/gun/GunData;)Z",
            at = @At("HEAD"),
            argsOnly = true,
            index = 1,
            require = 0
    )
    private static GunData tud$replaceGunData$isExplodeEnabled(GunData originalGunData, ItemStack gunItem) {
        GunData custom = TaczGunDataMap.getCustomGunData(gunItem, originalGunData);
        return custom != null ? custom : originalGunData;
    }


    @ModifyVariable(
            method = "getArmorIgnoreWithAttachment(Lnet/minecraft/world/item/ItemStack;Lcom/tacz/guns/resource/pojo/data/gun/GunData;)D",
            at = @At("HEAD"),
            argsOnly = true,
            index = 1,
            require = 0
    )
    private static GunData tud$replaceGunData$getArmorIgnoreWithAttachment(GunData originalGunData, ItemStack gunItem) {
        GunData custom = TaczGunDataMap.getCustomGunData(gunItem, originalGunData);
        return custom != null ? custom : originalGunData;
    }


    @ModifyVariable(
            method = "getHeadshotMultiplier(Lnet/minecraft/world/item/ItemStack;Lcom/tacz/guns/resource/pojo/data/gun/GunData;)D",
            at = @At("HEAD"),
            argsOnly = true,
            index = 1,
            require = 0
    )
    private static GunData tud$replaceGunData$getHeadshotMultiplier(GunData originalGunData, ItemStack gunItem) {
        GunData custom = TaczGunDataMap.getCustomGunData(gunItem, originalGunData);
        return custom != null ? custom : originalGunData;
    }


    @ModifyVariable(
            method = "getDamageWithAttachment(Lnet/minecraft/world/item/ItemStack;Lcom/tacz/guns/resource/pojo/data/gun/GunData;)D",
            at = @At("HEAD"),
            argsOnly = true,
            index = 1,
            require = 0
    )
    private static GunData tud$replaceGunData$getDamageWithAttachment(GunData originalGunData, ItemStack gunItem) {
        GunData custom = TaczGunDataMap.getCustomGunData(gunItem, originalGunData);
        return custom != null ? custom : originalGunData;
    }
}
