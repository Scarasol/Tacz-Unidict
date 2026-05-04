package com.scarasol.tud.data;

import com.scarasol.tud.TudMod;
import com.scarasol.tud.api.data.SearchableModData;
import com.scarasol.tud.api.serialization.JsonData;
import com.scarasol.tud.api.serialization.JsonTypeId;
import com.scarasol.tud.util.data.DataManager;
import com.tacz.guns.resource.pojo.data.gun.ExtraDamage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.loading.FMLPaths;

import javax.annotation.Nullable;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@JsonTypeId("tacz_unidict_ammo_data")
public class AmmoData implements JsonData, SearchableModData {

    private ResourceLocation ammoId;
    private ResourceLocation entityId;
    private boolean isItem;
    private Float speed;
    private Float gravity;
    private Float friction;
    private Float damage;
    private Float knockback;
    private Integer bulletAmount;
    private Float soundVolume;
    private Float soundPitch;
    private Boolean suppressSound;
    private Float soundDistanceMultiplier;
    private Float accuracyModifier;
    private Float horizontalRecoilModifier;
    private Float verticalRecoilModifier;
    private Boolean igniteEntity;
    private Boolean igniteBlock;
    private Integer igniteEntityTime;
    private Boolean explosion;
    private Float explosionDamage;
    private Float explosionRadius;
    private Integer explosionDelayCount;
    private Boolean explosionKnockback;
    private Boolean explosionDestroyBlock;
    private Integer pierce;
    private Boolean isTracerAmmo;
    private Float armorIgnore;
    private Float headShot;
    private List<EffectData> effectDataList;
    private LinkedList<ExtraDamage.DistanceDamagePair> damageAdjust;
    private String modifierId;

    public AmmoData() {
        effectDataList = new ArrayList<>();
    }

    public AmmoData(ResourceLocation ammoId) {
        this.ammoId = ammoId;
    }

    public AmmoData(ResourceLocation ammoId, Boolean isItem) {
        this.ammoId = ammoId;
        this.isItem = isItem;
    }

    public ResourceLocation getAmmoId() {
        return ammoId;
    }

    public void setAmmoId(ResourceLocation ammoId) {
        this.ammoId = ammoId;
    }

    public ResourceLocation getEntityId() {
        return entityId;
    }

    public void setEntityId(ResourceLocation entityId) {
        this.entityId = entityId;
    }

    public boolean isItem() {
        return isItem;
    }

    public void setIsItem(Boolean item) {
        isItem = item;
    }

    public Float getSpeed() {
        return speed;
    }

    public void setSpeed(Float speed) {
        this.speed = speed;
    }

    public Float getGravity() {
        return gravity;
    }

    public void setGravity(Float gravity) {
        this.gravity = gravity;
    }

    public Float getFriction() {
        return friction;
    }

    public void setFriction(Float friction) {
        this.friction = friction;
    }

    public Float getDamage() {
        return damage;
    }

    public void setDamageAmount(float damage) {
        this.damage = damage;
    }

    public Float getKnockback() {
        return knockback;
    }

    public void setKnockback(Float knockback) {
        this.knockback = knockback;
    }

    public Integer getBulletAmount() {
        return bulletAmount;
    }

    public void setBulletAmount(Integer bulletAmount) {
        this.bulletAmount = bulletAmount;
    }

    public Float getSoundVolume() {
        return soundVolume;
    }

    public Float getSoundPitch() {
        return soundPitch;
    }

    public Boolean getSuppressSound() {
        return suppressSound;
    }

    public Float getSoundDistanceMultiplier() {
        return soundDistanceMultiplier;
    }

    public Float getAccuracyModifier() {
        return accuracyModifier;
    }

    public void setAccuracyModifier(Float accuracyModifier) {
        this.accuracyModifier = accuracyModifier;
    }

    public Float getHorizontalRecoilModifier() { return horizontalRecoilModifier; }

    public void setHorizontalRecoilModifier(Float horizontalRecoilModifier) { this.horizontalRecoilModifier = horizontalRecoilModifier; }

    public Float getVerticalRecoilModifier() { return verticalRecoilModifier; }

    public void setVerticalRecoilModifier(Float verticalRecoilModifier) { this.verticalRecoilModifier = verticalRecoilModifier; }

    public Boolean getExplosion() {
        return explosion;
    }

    public void setExplosion(Boolean explosion) {
        this.explosion = explosion;
    }

    public Boolean getIgniteEntity() {
        return igniteEntity;
    }

    public void setIgniteEntity(Boolean igniteEntity) {
        this.igniteEntity = igniteEntity;
    }

    public Boolean getIgniteBlock() {
        return igniteBlock;
    }

    public void setIgniteBlock(Boolean igniteBlock) {
        this.igniteBlock = igniteBlock;
    }

    public Integer getIgniteEntityTime() {
        return igniteEntityTime;
    }

    public void setIgniteEntityTime(Integer igniteEntityTime) {
        this.igniteEntityTime = igniteEntityTime;
    }

    public Float getExplosionDamage() {
        return explosionDamage;
    }

    public void setExplosionDamage(Float explosionDamage) {
        this.explosionDamage = explosionDamage;
    }

    public Float getExplosionRadius() {
        return explosionRadius;
    }

    public void setExplosionRadius(Float explosionRadius) {
        this.explosionRadius = explosionRadius;
    }

    public Integer getExplosionDelayCount() {
        return explosionDelayCount;
    }

    public void setExplosionDelayCount(Integer explosionDelayCount) {
        this.explosionDelayCount = explosionDelayCount;
    }

    public Boolean getExplosionKnockback() {
        return explosionKnockback;
    }

    public void setExplosionKnockback(Boolean explosionKnockback) {
        this.explosionKnockback = explosionKnockback;
    }

    public Boolean getExplosionDestroyBlock() {
        return explosionDestroyBlock;
    }

    public void setExplosionDestroyBlock(Boolean explosionDestroyBlock) {
        this.explosionDestroyBlock = explosionDestroyBlock;
    }

    public Integer getPierce() {
        return pierce;
    }

    public void setPierce(Integer pierce) {
        this.pierce = pierce;
    }

    public Boolean getTracerAmmo() {
        return isTracerAmmo;
    }

    public void setTracerAmmo(Boolean tracerAmmo) {
        isTracerAmmo = tracerAmmo;
    }

    public Float getArmorIgnore() {
        return armorIgnore;
    }

    public void setArmorIgnore(Float armorIgnore) {
        this.armorIgnore = armorIgnore;
    }

    public Float getHeadShot() {
        return headShot;
    }

    public void setHeadShot(Float headShot) {
        this.headShot = headShot;
    }

    public List<EffectData> getEffectDataList() {
        return effectDataList;
    }

    public void setEffectDataList(List<EffectData> effectDataList) {
        this.effectDataList = effectDataList;
    }

    public LinkedList<ExtraDamage.DistanceDamagePair> getDamageAdjust() {
        return damageAdjust;
    }

    public void setDamageAdjust(LinkedList<ExtraDamage.DistanceDamagePair> damageAdjust) {
        this.damageAdjust = damageAdjust;
    }

    public String getModifierId() {
        return modifierId;
    }

    public void setModifierId(String modifierId) {
        this.modifierId = modifierId;
    }

    @Override
    public String getId() {
        return ammoId.toString();
    }

    @Override
    public void onLoaded() {
        DataManager.registerModData(this);
    }

    @Override
    public Path getPath() {
        return FMLPaths.CONFIGDIR.get().resolve(TudMod.MODID).resolve("ammo_data").resolve(getAmmoId().toString().replaceAll(":", "_") + ".json");
    }

    @Nullable
    public Tuple<ResourceLocation, Boolean> getAmmo() {
        if (ammoId == null) {
            return null;
        }
        return new Tuple<>(ammoId, isItem);
    }

}
