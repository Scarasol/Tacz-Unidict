package com.scarasol.tud.init;

import com.scarasol.tud.TudMod;
import com.scarasol.tud.entity.ShotFallingBlockEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * @author Scarasol
 */
public class TudEntities {
    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TudMod.MODID);

    public static final RegistryObject<EntityType<ShotFallingBlockEntity>> SHOT_FALLING_BLOCK = register("shot_falling_block_entity", EntityType.Builder.of(ShotFallingBlockEntity::new, MobCategory.MISC)
            .setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.98f, 0.98f));

    private static <T extends Entity> RegistryObject<EntityType<T>> register(String registryName, EntityType.Builder<T> entityTypeBuilder) {
        return REGISTRY.register(registryName, () -> entityTypeBuilder.build(registryName));
    }
}
