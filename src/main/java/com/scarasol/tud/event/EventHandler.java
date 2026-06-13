package com.scarasol.tud.event;

import com.scarasol.tud.TudMod;
import com.scarasol.tud.configuration.CommonConfig;
import com.scarasol.tud.data.*;
import com.scarasol.tud.manager.AmmoManager;
import com.scarasol.tud.util.data.DataManager;
import com.scarasol.tud.util.io.ModGson;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.crafting.GunSmithTableRecipe;
import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import javax.xml.crypto.Data;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Scarasol
 */
@Mod.EventBusSubscriber
public class EventHandler {


    private static final int DEFAULT_DURATION_TICKS = 200;

    @SubscribeEvent
    public static void onEntityHurtByGunPost(EntityHurtByGunEvent.Pre event) {
        if (event.getLogicalSide().isClient()) return;
        Entity hurt = event.getHurtEntity();
        if (hurt == null) return;
        Entity bullet = event.getBullet();
        String ammoId = bullet.getPersistentData().getString("TudAmmoId");
        AmmoData ammoData = DataManager.getSearchableModData(AmmoData.class, ammoId);
        if (ammoData == null) return;

        String modifierId = ammoData.getModifierId();
        if (modifierId != null) {
            ModifierData modifierData = DataManager.getSearchableModData(ModifierData.class, modifierId);
            if (modifierData != null) {
                event.setBaseAmount((float) (event.getBaseAmount() * modifierData.getModifier(hurt)));
            }
        }

        if (bullet instanceof Projectile projectile) {
            Entity owner = projectile.getOwner();
            if (owner instanceof LivingEntity shooter) {
                ItemStack gunItem = shooter.getMainHandItem();
                if (gunItem.getItem() instanceof IGun) {
                    com.scarasol.tud.data.GunData gunData = AmmoManager.getGunData(gunItem);
                    if (gunData != null) {
                        MagData magData = gunData.getCurrentMag(gunItem);
                        if (magData != null && magData.getDamageBonus() != 0f) {
                            float newDamage = event.getBaseAmount() + magData.getDamageBonus();
                            if (newDamage < 0) newDamage = 0;
                            event.setBaseAmount(newDamage);
                        }
                    }
                }
            }
        }
    }


    @SubscribeEvent
    public static void onEntityHurtByGunPost(EntityHurtByGunEvent.Post event) {
        if (event.getLogicalSide().isClient()) {
            return;
        }

        Entity hurt = event.getHurtEntity();
        if (!(hurt instanceof LivingEntity living)) {
            return;
        }
        Entity bullet = event.getBullet();
        String ammoId = bullet.getPersistentData().getString("TudAmmoId");

        AmmoData ammoData = DataManager.getSearchableModData(AmmoData.class, ammoId);
        if (ammoData == null) {
            return;
        }

        List<EffectData> list = ammoData.getEffectDataList();
        if (list == null || list.isEmpty()) {
            return;
        }

        applyEffects(living, list);
    }

    private static void applyEffects(LivingEntity target, List<EffectData> list) {
        for (EffectData data : list) {
            if (data == null || data.resourceLocation() == null) {
                continue;
            }

            ResourceLocation effectId = data.resourceLocation();
            @Nullable MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(effectId);
            if (effect == null) {
                continue;
            }

            int amplifier = data.amplifier() == null ? 0 : Math.max(0, data.amplifier());
            int duration = data.duration() == null ? DEFAULT_DURATION_TICKS : Math.max(1, data.duration() * 20);

            target.addEffect(new MobEffectInstance(effect, duration, amplifier));
        }
    }

    @SubscribeEvent
    public static void loadData(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) {
            return;
        }
        try {
            DataManager.clear(GunData.class);
            DataManager.clear(AmmoData.class);
            DataManager.clear(TaczGunDataMap.class);
            DataManager.clear(ModifierData.class);
            ModGson.INSTANCE.loadAll(FMLPaths.CONFIGDIR.get().resolve(TudMod.MODID).resolve("modifier_data"));
            ModGson.INSTANCE.loadAll(FMLPaths.CONFIGDIR.get().resolve(TudMod.MODID).resolve("gun_data"));
            ModGson.INSTANCE.loadAll(FMLPaths.CONFIGDIR.get().resolve(TudMod.MODID).resolve("ammo_data"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public static void loadData(ServerStartedEvent event) {
        try {
            DataManager.clear(GunData.class);
            DataManager.clear(AmmoData.class);
            DataManager.clear(TaczGunDataMap.class);
            DataManager.clear(ModifierData.class);
            ModGson.INSTANCE.loadAll(FMLPaths.CONFIGDIR.get().resolve(TudMod.MODID).resolve("modifier_data"));
            ModGson.INSTANCE.loadAll(FMLPaths.CONFIGDIR.get().resolve(TudMod.MODID).resolve("gun_data"));
            ModGson.INSTANCE.loadAll(FMLPaths.CONFIGDIR.get().resolve(TudMod.MODID).resolve("ammo_data"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
