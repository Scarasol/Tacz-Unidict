package com.scarasol.tud.mixin.accessor;

import com.tacz.guns.resource.pojo.data.gun.BulletData;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author Scarasol
 */
@Mixin(GunData.class)
public interface GunDataAccessor {
    @Accessor("bulletData")
    void tud$setBulletData(BulletData v);

    @Accessor("roundsPerMinute")
    void tud$setRoundsPerMinute(int v);

    @Accessor("ammoAmount")
    void tud$setAmmoAmount(int v);

    @Accessor("extendedMagAmmoAmount")
    void tud$setExtendedMagAmmoAmount(int[] v);

    @Accessor("ammoId")
    void tud$setAmmoId(ResourceLocation v);
}
