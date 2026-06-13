package com.scarasol.tud.data;

import net.minecraft.resources.ResourceLocation;

public class MagData {
    private final ResourceLocation ammoId;
    private final Integer ammoAmount;
    private final Integer roundsPerMinute;
    private final Integer[] extendedMagAmmoAmount;
    private final boolean allowSuppress;
    private final Float accuracyModifier;
    private final Float horizontalRecoilModifier;
    private final Float verticalRecoilModifier;
    private final Float damageBonus;

    public MagData(ResourceLocation ammoId, Integer ammoAmount, Integer roundsPerMinute, Integer[] extendedMagAmmoAmount) {
        this(ammoId, ammoAmount, roundsPerMinute, extendedMagAmmoAmount, true);
    }

    public MagData(ResourceLocation ammoId, Integer ammoAmount, Integer roundsPerMinute,
                   Integer[] extendedMagAmmoAmount, boolean allowSuppress) {
        this(ammoId, ammoAmount, roundsPerMinute, extendedMagAmmoAmount, allowSuppress, 1.0f);
    }

    public MagData(ResourceLocation ammoId, Integer ammoAmount, Integer roundsPerMinute,
                   Integer[] extendedMagAmmoAmount, boolean allowSuppress, Float accuracyModifier) {
        this(ammoId, ammoAmount, roundsPerMinute, extendedMagAmmoAmount, allowSuppress, accuracyModifier, 1.0f, 1.0f);
    }

    public MagData(ResourceLocation ammoId, Integer ammoAmount, Integer roundsPerMinute,
                   Integer[] extendedMagAmmoAmount, boolean allowSuppress, Float accuracyModifier,
                   Float horizontalRecoilModifier, Float verticalRecoilModifier) {
        this(ammoId, ammoAmount, roundsPerMinute, extendedMagAmmoAmount, allowSuppress,
                accuracyModifier, horizontalRecoilModifier, verticalRecoilModifier, 0f);
    }

    public MagData(ResourceLocation ammoId, Integer ammoAmount, Integer roundsPerMinute,
                   Integer[] extendedMagAmmoAmount, boolean allowSuppress, Float accuracyModifier,
                   Float horizontalRecoilModifier, Float verticalRecoilModifier, Float damageBonus) {
        this.ammoId = ammoId;
        this.ammoAmount = ammoAmount;
        this.roundsPerMinute = roundsPerMinute;
        this.extendedMagAmmoAmount = extendedMagAmmoAmount;
        this.allowSuppress = allowSuppress;
        this.accuracyModifier = accuracyModifier;
        this.horizontalRecoilModifier = horizontalRecoilModifier;
        this.verticalRecoilModifier = verticalRecoilModifier;
        this.damageBonus = damageBonus;
    }

    public ResourceLocation ammoId() { return ammoId; }
    public Integer ammoAmount() { return ammoAmount; }
    public Integer roundsPerMinute() { return roundsPerMinute; }
    public Integer[] extendedMagAmmoAmount() { return extendedMagAmmoAmount; }
    public boolean allowSuppress() { return allowSuppress; }

    public float getAccuracyModifier() {
        return accuracyModifier == null ? 1.0f : accuracyModifier;
    }

    public float getHorizontalRecoilModifier() {
        return horizontalRecoilModifier == null ? 1.0f : horizontalRecoilModifier;
    }

    public float getVerticalRecoilModifier() {
        return verticalRecoilModifier == null ? 1.0f : verticalRecoilModifier;
    }

    public float getDamageBonus() {
        return damageBonus == null ? 0f : damageBonus;
    }
}