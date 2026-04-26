package com.scarasol.tud.api.functional;

import com.scarasol.tud.api.data.ModData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;


/**
 * @author Scarasol
 */
@FunctionalInterface
public interface EntityGetter extends ModData {
    Entity spawnEntity(ServerLevel serverLevel, ResourceLocation entityId);
}
