package com.scarasol.tud.mixin.accessor;

import com.tacz.guns.resource.pojo.data.gun.BulletData;
import com.tacz.guns.resource.pojo.data.gun.ExplosionData;
import com.tacz.guns.resource.pojo.data.gun.ExtraDamage;
import com.tacz.guns.resource.pojo.data.gun.Ignite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author Scarasol
 */
@Mixin(BulletData.class)
public interface BulletDataAccessor {
    @Accessor("lifeSecond") void tud$setLifeSecond(float v);
    @Accessor("bulletAmount") void tud$setBulletAmount(int v);
    @Accessor("damageAmount") void tud$setDamageAmount(float v);
    @Accessor("speed") void tud$setSpeed(float v);
    @Accessor("gravity") void tud$setGravity(float v);
    @Accessor("knockback") void tud$setKnockback(float v);
    @Accessor("friction") void tud$setFriction(float v);
    @Accessor("pierce") void tud$setPierce(int v);
    @Accessor("ignite") void tud$setIgnite(Ignite v);
    @Accessor("igniteEntityTime") void tud$setIgniteEntityTime(int v);
    @Accessor("tracerCountInterval") void tud$setTracerCountInterval(int v);
    @Accessor("extraDamage") void tud$setExtraDamage(ExtraDamage v);
    @Accessor("explosionData") void tud$setExplosionData(ExplosionData v);
}
