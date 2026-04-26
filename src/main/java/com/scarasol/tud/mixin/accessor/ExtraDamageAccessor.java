package com.scarasol.tud.mixin.accessor;

import com.tacz.guns.resource.pojo.data.gun.ExtraDamage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.LinkedList;

/**
 * @author Scarasol
 */
@Mixin(ExtraDamage.class)
public interface ExtraDamageAccessor {
    @Accessor("armorIgnore") void tud$setArmorIgnore(float v);
    @Accessor("headShotMultiplier") void tud$setHeadShotMultiplier(float v);
    @Accessor("damageAdjust") void tud$setDamageAdjust(LinkedList<ExtraDamage.DistanceDamagePair> v);
}
