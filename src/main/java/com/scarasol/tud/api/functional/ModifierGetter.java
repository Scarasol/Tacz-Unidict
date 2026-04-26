package com.scarasol.tud.api.functional;

import com.scarasol.tud.api.data.ModData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.Map;

/**
 * @author Scarasol
 */
@FunctionalInterface
public interface ModifierGetter extends ModData {
    double getModifier(Entity entity, Map<String, Double> modifierMap);
}
