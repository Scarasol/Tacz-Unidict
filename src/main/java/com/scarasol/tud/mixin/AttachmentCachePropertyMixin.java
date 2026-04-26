package com.scarasol.tud.mixin;

import com.scarasol.tud.data.TaczGunDataMap;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = AttachmentCacheProperty.class)
public abstract class AttachmentCachePropertyMixin {

    @ModifyVariable(method = "eval", at = @At("HEAD"), index = 2, remap = false)
    private GunData tud$replaceGunData(GunData originalGunData, ItemStack gunItem) {
        GunData custom = TaczGunDataMap.getCustomGunData(gunItem, originalGunData);
        return custom != null ? custom : originalGunData;
    }
}
