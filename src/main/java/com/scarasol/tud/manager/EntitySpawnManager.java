package com.scarasol.tud.manager;

import com.scarasol.tud.TudMod;
import com.scarasol.tud.api.functional.EntityGetter;
import com.scarasol.tud.data.AmmoData;
import com.scarasol.tud.entity.ShotFallingBlockEntity;
import com.scarasol.tud.util.data.DataManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * @author Scarasol
 */
public class EntitySpawnManager {

    public static Entity spawnEntity(ServerLevel serverLevel, ResourceLocation resourceLocationId) {
        EntityType entityType = ForgeRegistries.ENTITY_TYPES.getValue(resourceLocationId);
        if (entityType == null) {
            return null;
        }
        return entityType.create(serverLevel);
    }

    public static Entity spawnEntityByTag(ServerLevel serverLevel, ResourceLocation resourceLocationId) {
        TagKey<EntityType<?>> tagKey = TagKey.create(Registries.ENTITY_TYPE, resourceLocationId);
        EntityType entityType = ForgeRegistries.ENTITY_TYPES.tags()
                .getTag(tagKey).getRandomElement(serverLevel.getRandom())
                .orElse(null);
        if (entityType == null) {
            return null;
        }
        return entityType.create(serverLevel);
    }

    public static FallingBlockEntity spawnFallingBlockEntity(ServerLevel serverLevel, ResourceLocation resourceLocationId) {
        Block block = ForgeRegistries.BLOCKS.getValue(resourceLocationId);
        if (block == null || block instanceof AirBlock) {
            return null;
        }
        return ShotFallingBlockEntity.create(serverLevel, block.defaultBlockState());
    }

    public static FallingBlockEntity spawnFallingBlockEntityByTag(ServerLevel serverLevel, ResourceLocation resourceLocationId) {
        TagKey<Block> tagKey = TagKey.create(Registries.BLOCK, resourceLocationId);
        Block block = ForgeRegistries.BLOCKS.tags()
                .getTag(tagKey).getRandomElement(serverLevel.getRandom())
                .orElse(null);
        if (block == null || block instanceof AirBlock) {
            return null;
        }
        return ShotFallingBlockEntity.create(serverLevel, block.defaultBlockState());
    }

    public static void registerEntityGetter() {
        DataManager.registerModData(EntityGetter.class, EntitySpawnManager::spawnEntity);
        DataManager.registerModData(EntityGetter.class, EntitySpawnManager::spawnEntityByTag);
        DataManager.registerModData(EntityGetter.class, EntitySpawnManager::spawnFallingBlockEntity);
        DataManager.registerModData(EntityGetter.class, EntitySpawnManager::spawnFallingBlockEntityByTag);
    }

    @Nullable
    public static Entity getEntity(ServerLevel serverLevel, AmmoData ammoData) {
        return DataManager.getModDataRegisterData(EntityGetter.class)
                .stream()
                .map(entityGetter -> entityGetter.spawnEntity(serverLevel, ammoData.getEntityId()))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public static void shootEntity(Entity entity, float processedSpeed, float inaccuracy, float pitch, float yaw) {
        Vec3 dir = calculateViewVector(pitch, yaw);
        if (entity instanceof Projectile projectile) {
            projectile.shoot(dir.x, dir.y, dir.z, processedSpeed, inaccuracy);
        } else {
            shoot(entity, dir.x, dir.y, dir.z, processedSpeed, inaccuracy);
        }
    }

    public static void setPos(LivingEntity throwerIn, Entity entity) {
        Vec3 vec3 = throwerIn.getLookAngle();
        vec3 = vec3.scale(2.5);

        double posX = vec3.x + throwerIn.xOld + (throwerIn.getX() - throwerIn.xOld) / 2.0D;
        double posY = vec3.y + throwerIn.yOld + (throwerIn.getY() - throwerIn.yOld) / 2.0D + (double)throwerIn.getEyeHeight();
        double posZ = vec3.z + throwerIn.zOld + (throwerIn.getZ() - throwerIn.zOld) / 2.0D;
        entity.setPos(posX, posY, posZ);
        if (entity instanceof FallingBlockEntity fallingBlockEntity) {
            fallingBlockEntity.xo = posX;
            fallingBlockEntity.yo = posY;
            fallingBlockEntity.zo = posZ;
            fallingBlockEntity.setStartPos(throwerIn.blockPosition());
        }
    }

    public static void shoot(Entity entity, double dx, double dy, double dz, float speed, float inaccuracy) {
        Vec3 vec3 = (new Vec3(dx, dy, dz)).normalize().add(entity.level().random.triangle(0.0D, 0.0172275D * (double)inaccuracy), entity.level().random.triangle(0.0D, 0.0172275D * (double)inaccuracy), entity.level().random.triangle(0.0D, 0.0172275D * (double)inaccuracy)).scale(speed);
        entity.setDeltaMovement(vec3);
        double d0 = vec3.horizontalDistance();
        entity.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * (double)(180F / (float)Math.PI)));
        entity.setXRot((float)(Mth.atan2(vec3.y, d0) * (double)(180F / (float)Math.PI)));
        entity.yRotO = entity.getYRot();
        entity.xRotO = entity.getXRot();
    }

    public static Vec3 calculateViewVector(float pitch, float yaw) {
        float f = pitch * ((float)Math.PI / 180F);
        float f1 = -yaw * ((float)Math.PI / 180F);
        float f2 = Mth.cos(f1);
        float f3 = Mth.sin(f1);
        float f4 = Mth.cos(f);
        float f5 = Mth.sin(f);
        return new Vec3(f3 * f4, -f5, f2 * f4);
    }
}
