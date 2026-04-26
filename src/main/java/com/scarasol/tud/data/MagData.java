package com.scarasol.tud.data;

import net.minecraft.resources.ResourceLocation;

public record MagData(ResourceLocation ammoId, Integer ammoAmount, Integer roundsPerMinute, Integer[] extendedMagAmmoAmount) {
}
