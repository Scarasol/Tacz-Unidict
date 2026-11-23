package com.scarasol.tud.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.scarasol.tud.data.AmmoData;
import com.scarasol.tud.data.TaczGunDataMap;
import com.scarasol.tud.manager.AmmoManager;
import com.scarasol.tud.manager.EntitySpawnManager;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import com.tacz.guns.resource.pojo.data.gun.BulletData;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * @author Scarasol
 */
@Mixin(value = ModernKineticGunScriptAPI.class)
public abstract class ModernKineticGunScriptAPIMixin {

    @Shadow private ItemStack itemStack;
    @Shadow private LivingEntity shooter;
    @Shadow private ResourceLocation gunId;
    @ModifyVariable(
            method = "shootOnce",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lcom/tacz/guns/resource/index/CommonGunIndex;getGunData()Lcom/tacz/guns/resource/pojo/data/gun/GunData;"
            ),remap = false
    )
    private GunData tud$replaceShootOnceGunData(GunData originalGunData) {
        if (originalGunData == null || itemStack == null || itemStack.isEmpty()) {
            return originalGunData;
        }
        GunData custom = TaczGunDataMap.getCustomGunData(itemStack, originalGunData);
        return custom != null ? custom : originalGunData;
    }


    @ModifyVariable(
            method = "shootOnce",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lcom/tacz/guns/resource/index/CommonGunIndex;getBulletData()Lcom/tacz/guns/resource/pojo/data/gun/BulletData;"
            ),remap = false
    )
    private BulletData tud$replaceShootOnceBulletData(BulletData originalBulletData) {
        if (itemStack == null || itemStack.isEmpty()) {
            return originalBulletData;
        }
        GunData originalGunData = null;
        GunData customGunData = TaczGunDataMap.getCustomGunData(itemStack, originalGunData);
        if (customGunData != null) {
            BulletData bd = customGunData.getBulletData();
            if (bd != null) {
                return bd;
            }
        }
        return originalBulletData;
    }

    @Inject(
            method = "lambda$hasAmmoToConsume$6",
            cancellable = true,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;", ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void tud$hasAmmoToConsume(IItemHandler cap, CallbackInfoReturnable<Boolean> cir, int i, ItemStack checkAmmoStack) {
        Item ammo = checkAmmoStack.getItem();
        if (ammo instanceof IAmmo || ammo instanceof IAmmoBox) {
            return;
        }
        if (AmmoManager.isAmmoOfGunItem(itemStack, checkAmmoStack)) {
            cir.setReturnValue(true);
        }
    }


    @Inject(
            method = "lambda$shootOnce$2",
            at = @At(value = "INVOKE", target = "Lcom/tacz/guns/resource/pojo/data/gun/GunData;getAmmoId()Lnet/minecraft/resources/ResourceLocation;"),
            remap = false
    )
    private void tud$spawnCustomProjectiles(CallbackInfoReturnable<Boolean> cir,
                                           @Local(argsOnly = true) GunData gunData,
                                           @Local(argsOnly = true, ordinal = 0) LocalIntRef bulletAmount,
                                           @Local(argsOnly = true, ordinal = 1) float processedSpeed,
                                           @Local(argsOnly = true, ordinal = 2) float inaccuracy,
                                           @Local(ordinal = 3) float pitch,
                                           @Local(ordinal = 4) float yaw) {
        ResourceLocation ammoId = gunData.getAmmoId();
        if (ammoId == null) {
            return;
        }

        if (!AmmoManager.canUseGeneralAmmo(gunId.toString(), ammoId.toString())) {
            return;
        }

        AmmoData ammoData = AmmoManager.getCurrentAmmoData(itemStack);

        if (ammoData == null || ammoData.getEntityId() == null) {
            return;
        }

        if (!(shooter.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        for (int i = 0, count = bulletAmount.get(); i < count; ++i) {
            Entity entity = EntitySpawnManager.getEntity(serverLevel, ammoData);

            if (entity == null) {
                continue;
            }

            EntitySpawnManager.setPos(shooter, entity);
            EntitySpawnManager.shootEntity(entity, processedSpeed, inaccuracy, pitch, yaw);
            serverLevel.addFreshEntity(entity);
        }

        // Skip the kinetic-bullet loop and leave shot sound and post-fire dispatch to TaCZ.
        bulletAmount.set(0);
    }


    @WrapOperation(
            method = "lambda$shootOnce$2",
            at = @At(value = "INVOKE", target = "Lcom/tacz/guns/entity/EntityKineticBullet;applyShotgunDamageSpread(I)V"),
            remap = false
    )
    private void tud$tagBullet(EntityKineticBullet bullet, int bulletAmount, Operation<Void> original) {
        AmmoData ammoData = AmmoManager.getCurrentAmmoData(itemStack);
        if (ammoData != null) {
            ResourceLocation resourceLocation = ammoData.getAmmoId();
            bullet.getPersistentData().putString("TudAmmoId", resourceLocation.toString());
        }
        original.call(bullet, bulletAmount);
    }

}
