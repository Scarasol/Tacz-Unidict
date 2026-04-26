package com.scarasol.tud.util;

import com.scarasol.tud.data.AmmoData;
import com.scarasol.tud.data.MagData;
import com.scarasol.tud.manager.AmmoManager;
import com.scarasol.tud.mixin.accessor.BulletDataAccessor;
import com.scarasol.tud.mixin.accessor.ExtraDamageAccessor;
import com.scarasol.tud.mixin.accessor.GunDataAccessor;
import com.scarasol.tud.util.data.DataManager;
import com.scarasol.tud.api.functional.EntityGetter;
import com.scarasol.tud.manager.EntitySpawnManager;
import com.tacz.guns.resource.pojo.data.gun.BulletData;
import com.tacz.guns.resource.pojo.data.gun.ExplosionData;
import com.tacz.guns.resource.pojo.data.gun.ExtraDamage;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.resource.pojo.data.gun.Ignite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;

public final class TaczGunDataOverrideUtil {

    private static final Field[] GUN_DATA_FIELDS = collectInstanceFields(GunData.class);

    private TaczGunDataOverrideUtil() {
    }

    @Nullable
    public static GunData buildOverrideGunData(GunData originalGunData, ItemStack gunItem) {
        AmmoData ammo = AmmoManager.getCurrentAmmoData(gunItem);
        MagData mag = AmmoManager.getCurrentMagData(gunItem);

        if (ammo == null && mag == null) {
            return null;
        }

        // 只有当 ammo.isItem == false 时，才允许覆盖 ammoId，并且只用 ammoData 的 ammoId
        boolean ammoIdOverride = false;
        ResourceLocation desiredAmmoId = null;
        if (ammo != null && !ammo.isItem() && ammo.getAmmoId() != null) {
            desiredAmmoId = ammo.getAmmoId();
            ammoIdOverride = desiredAmmoId != null
                    && (originalGunData.getAmmoId() == null || !desiredAmmoId.equals(originalGunData.getAmmoId()));
        }

        boolean ammoOverride = ammo != null && needOverride(ammo);
        boolean magOverride = mag != null && needOverride(mag);

        if (!ammoOverride && !magOverride && !ammoIdOverride) {
            return null;
        }

        GunData copy = shallowCopyGunData(originalGunData);
        if (copy == null) {
            return null;
        }

        if (ammoIdOverride) {
            ((GunDataAccessor) (Object) copy).tud$setAmmoId(desiredAmmoId);
        }

        if (magOverride) {
            applyMagOverrides(copy, mag);
        }

        if (ammoOverride) {
            BulletData srcBullet = originalGunData.getBulletData();
            if (srcBullet != null) {
                BulletData bulletCopy = buildBulletCopy(srcBullet, ammo);
                ((GunDataAccessor) (Object) copy).tud$setBulletData(bulletCopy);
            }
        }

        return copy;
    }

    private static boolean needOverride(AmmoData ammo) {
        return ammo.getDamage() != null
                || ammo.getSpeed() != null
                || ammo.getGravity() != null
                || ammo.getFriction() != null
                || ammo.getKnockback() != null
                || ammo.getPierce() != null
                || ammo.getIgniteEntity() != null
                || ammo.getIgniteBlock() != null
                || ammo.getIgniteEntityTime() != null
                || ammo.getTracerAmmo() != null
                || ammo.getExplosion() != null
                || ammo.getExplosionRadius() != null
                || ammo.getExplosionDamage() != null
                || ammo.getExplosionKnockback() != null
                || ammo.getExplosionDestroyBlock() != null
                || ammo.getExplosionDelayCount() != null
                || ammo.getArmorIgnore() != null
                || ammo.getHeadShot() != null
                || (ammo.getDamageAdjust() != null && !ammo.getDamageAdjust().isEmpty());
    }

    private static boolean needOverride(MagData mag) {
        Integer[] ext = mag.extendedMagAmmoAmount();
        boolean extOverride = ext != null && ext.length > 0;
        return mag.ammoAmount() != null || mag.roundsPerMinute() != null || extOverride;
    }

    @Nullable
    private static GunData shallowCopyGunData(GunData src) {
        GunData dst = new GunData();
        try {
            shallowCopyFields(GUN_DATA_FIELDS, src, dst);
            return dst;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static void applyMagOverrides(GunData copy, MagData mag) {
        GunDataAccessor g = (GunDataAccessor) (Object) copy;

        Integer ammoAmount = mag.ammoAmount();
        if (ammoAmount != null) {
            g.tud$setAmmoAmount(Math.max(1, ammoAmount));
        }

        Integer rpm = mag.roundsPerMinute();
        if (rpm != null) {
            g.tud$setRoundsPerMinute(Math.max(1, rpm));
        }

        Integer[] ext = mag.extendedMagAmmoAmount();
        if (ext != null && ext.length > 0) {
            boolean anyNonNull = false;
            for (Integer v : ext) {
                if (v != null) {
                    anyNonNull = true;
                    break;
                }
            }
            if (anyNonNull) {
                int[] base = copy.getExtendedMagAmmoAmount();
                int fallback = Math.max(1, copy.getAmmoAmount());

                int[] result = new int[ext.length];
                for (int i = 0; i < ext.length; i++) {
                    Integer v = ext[i];
                    if (v != null) {
                        result[i] = Math.max(1, v);
                    } else if (base != null && i < base.length) {
                        result[i] = Math.max(1, base[i]);
                    } else {
                        result[i] = fallback;
                    }
                }

                g.tud$setExtendedMagAmmoAmount(result);
            }
        }
    }

    private static BulletData buildBulletCopy(BulletData srcBullet, AmmoData ammo) {
        BulletData dst = new BulletData();
        BulletDataAccessor b = (BulletDataAccessor) (Object) dst;

        inheritBullet(b, srcBullet);
        applyBulletOverrides(b, srcBullet, ammo);

        return dst;
    }

    private static void inheritBullet(BulletDataAccessor b, BulletData src) {
        b.tud$setLifeSecond(src.getLifeSecond());
        b.tud$setBulletAmount(src.getBulletAmount());
        b.tud$setDamageAmount(src.getDamageAmount());
        b.tud$setSpeed(src.getSpeed());
        b.tud$setGravity(src.getGravity());
        b.tud$setKnockback(src.getKnockback());
        b.tud$setFriction(src.getFriction());
        b.tud$setPierce(src.getPierce());
        b.tud$setIgnite(src.getIgnite());
        b.tud$setIgniteEntityTime(src.getIgniteEntityTime());
        b.tud$setTracerCountInterval(src.getTracerCountInterval());
        b.tud$setExplosionData(src.getExplosionData());
        b.tud$setExtraDamage(src.getExtraDamage());
    }

    private static void applyBulletOverrides(BulletDataAccessor b, BulletData srcBullet, AmmoData ammo) {
        if (ammo.getDamage() != null) {
            b.tud$setDamageAmount(Math.max(0.0F, ammo.getDamage()));
        }
        if (ammo.getSpeed() != null) {
            b.tud$setSpeed(Math.max(0.0F, ammo.getSpeed()));
        }
        if (ammo.getGravity() != null) {
            b.tud$setGravity(Math.max(0.0F, ammo.getGravity()));
        }
        if (ammo.getFriction() != null) {
            b.tud$setFriction(Math.max(0.0F, ammo.getFriction()));
        }
        if (ammo.getKnockback() != null) {
            b.tud$setKnockback(Math.max(0.0F, ammo.getKnockback()));
        }
        if (ammo.getPierce() != null) {
            b.tud$setPierce(Math.max(1, ammo.getPierce()));
        }

        applyExtraDamage(b, srcBullet, ammo);
        applyIgnite(b, srcBullet, ammo);
        applyExplosion(b, srcBullet, ammo);
        applyTracer(b, ammo);
    }

    /**
     * 修改点：
     * - 当 ammo.damage != null 时，复制旧 ExtraDamage 时不再复制 damageAdjust（保持 null）
     * - 但如果 ammo.damageAdjust 本身非空，仍然会正常覆盖写入
     */
    private static void applyExtraDamage(BulletDataAccessor b, BulletData srcBullet, AmmoData ammo) {
        boolean overrideArmorIgnore = ammo.getArmorIgnore() != null;
        boolean overrideHeadShot = ammo.getHeadShot() != null;
        LinkedList<ExtraDamage.DistanceDamagePair> overrideAdjust = ammo.getDamageAdjust();
        boolean overrideDamageAdjust = overrideAdjust != null && !overrideAdjust.isEmpty();

        ExtraDamage srcExtra = srcBullet.getExtraDamage();


        boolean stripDamageAdjustWhenDamageOverride = ammo.getDamage() != null && srcExtra != null;


        if (!overrideArmorIgnore && !overrideHeadShot && !overrideDamageAdjust && !stripDamageAdjustWhenDamageOverride) {
            return;
        }

        ExtraDamage extraCopy = new ExtraDamage();
        ExtraDamageAccessor e = (ExtraDamageAccessor) (Object) extraCopy;

        if (srcExtra != null) {
            e.tud$setArmorIgnore(srcExtra.getArmorIgnore());
            e.tud$setHeadShotMultiplier(srcExtra.getHeadShotMultiplier());

            if (stripDamageAdjustWhenDamageOverride) {
                e.tud$setDamageAdjust(new LinkedList<>());
            } else {
                var adj = srcExtra.getDamageAdjust();
                e.tud$setDamageAdjust(adj == null ? new LinkedList<>() : new LinkedList<>(adj));
            }
        } else {
            // srcExtra 不存在：只有当需要覆盖时才创建；damageAdjust 默认仍给空表（保持你原行为）
            e.tud$setDamageAdjust(new LinkedList<>());
        }

        if (overrideArmorIgnore) {
            e.tud$setArmorIgnore(Mth.clamp(ammo.getArmorIgnore(), 0.0F, 1.0F));
        }
        if (overrideHeadShot) {
            e.tud$setHeadShotMultiplier(Math.max(0.0F, ammo.getHeadShot()));
        }
        if (overrideDamageAdjust) {
            LinkedList<ExtraDamage.DistanceDamagePair> copied = new LinkedList<>();
            for (ExtraDamage.DistanceDamagePair p : overrideAdjust) {
                if (p != null) {
                    copied.add(new ExtraDamage.DistanceDamagePair(p.getDistance(), p.getDamage()));
                }
            }
            if (!copied.isEmpty()) {
                e.tud$setDamageAdjust(copied);
            }
        }

        b.tud$setExtraDamage(extraCopy);
    }

    private static void applyIgnite(BulletDataAccessor b, BulletData srcBullet, AmmoData ammo) {
        if (ammo.getIgniteEntity() == null && ammo.getIgniteBlock() == null && ammo.getIgniteEntityTime() == null) {
            return;
        }

        if (ammo.getIgniteEntity() != null || ammo.getIgniteBlock() != null) {
            Ignite old = srcBullet.getIgnite();
            boolean entity = ammo.getIgniteEntity() != null ? ammo.getIgniteEntity() : (old != null && old.isIgniteEntity());
            boolean block = ammo.getIgniteBlock() != null ? ammo.getIgniteBlock() : (old != null && old.isIgniteBlock());
            b.tud$setIgnite(new Ignite(entity, block));
        }

        if (ammo.getIgniteEntityTime() != null) {
            b.tud$setIgniteEntityTime(Math.max(0, ammo.getIgniteEntityTime()));
        }
    }

    private static void applyExplosion(BulletDataAccessor b, BulletData srcBullet, AmmoData ammo) {
        if (ammo.getExplosion() == null
                && ammo.getExplosionRadius() == null
                && ammo.getExplosionDamage() == null
                && ammo.getExplosionKnockback() == null
                && ammo.getExplosionDestroyBlock() == null
                && ammo.getExplosionDelayCount() == null) {
            return;
        }

        ExplosionData old = srcBullet.getExplosionData();

        boolean explode = ammo.getExplosion() != null ? ammo.getExplosion() : (old != null && old.isExplode());
        float radius = ammo.getExplosionRadius() != null ? Math.max(0.0F, ammo.getExplosionRadius()) : (old != null ? old.getRadius() : 0.0F);
        float damage = ammo.getExplosionDamage() != null ? Math.max(0.0F, ammo.getExplosionDamage()) : (old != null ? old.getDamage() : 0.0F);
        boolean knockback = ammo.getExplosionKnockback() != null ? ammo.getExplosionKnockback() : (old != null && old.isKnockback());
        boolean destroyBlock = ammo.getExplosionDestroyBlock() != null ? ammo.getExplosionDestroyBlock() : (old != null && old.isDestroyBlock());

        float delaySeconds;
        if (ammo.getExplosionDelayCount() != null) {
            delaySeconds = Math.max(0.0F, ammo.getExplosionDelayCount());
        } else {
            delaySeconds = old != null ? old.getDelay() : 30.0F;
        }

        b.tud$setExplosionData(new ExplosionData(explode, radius, damage, knockback, delaySeconds, destroyBlock));
    }

    private static void applyTracer(BulletDataAccessor b, AmmoData ammo) {
        if (ammo.getTracerAmmo() == null) {
            return;
        }
        b.tud$setTracerCountInterval(ammo.getTracerAmmo() ? 0 : -1);
    }

    private static Field[] collectInstanceFields(Class<?> c) {
        Field[] all = c.getDeclaredFields();
        int n = 0;
        for (Field f : all) {
            if (!Modifier.isStatic(f.getModifiers())) {
                n++;
            }
        }
        Field[] out = new Field[n];
        int i = 0;
        for (Field f : all) {
            if (Modifier.isStatic(f.getModifiers())) {
                continue;
            }
            f.setAccessible(true);
            out[i++] = f;
        }
        return out;
    }

    private static void shallowCopyFields(Field[] fields, Object src, Object dst) throws IllegalAccessException {
        for (Field f : fields) {
            f.set(dst, f.get(src));
        }
    }
}
