package com.scarasol.tud.event;

import com.scarasol.tud.TudMod;
import com.scarasol.tud.data.*;
import com.scarasol.tud.manager.AmmoManager;
import com.scarasol.tud.network.NetworkHandler;
import com.scarasol.tud.network.SyncAmmoDataPacket;
import com.scarasol.tud.util.data.DataManager;
import com.scarasol.tud.util.io.ModGson;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

/**
 * @author Scarasol
 */
@Mod.EventBusSubscriber
public class EventHandler {


    private static final int DEFAULT_DURATION_TICKS = 200;
    private static List<SyncAmmoDataPacket> ammoSyncPackets = List.of();

    @SubscribeEvent
    public static void onEntityHurtByGunPost(EntityHurtByGunEvent.Pre event) {
        if (event.getLogicalSide().isClient()) {
            return;
        }

        Entity hurt = event.getHurtEntity();
        if (hurt == null) {
            return;
        }

        Entity bullet = event.getBullet();
        String ammoId = bullet.getPersistentData().getString("TudAmmoId");

        AmmoData ammoData = DataManager.getSearchableModData(AmmoData.class, ammoId);
        if (ammoData == null) {
            return;
        }

        String modifierId = ammoData.getModifierId();
        if (modifierId == null) {
            return;
        }
        ModifierData modifierData = DataManager.getSearchableModData(ModifierData.class, modifierId);
        if (modifierData == null) {
            return;
        }
        event.setBaseAmount((float) (event.getBaseAmount() * modifierData.getModifier(hurt)));
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
    public static void syncAmmoData(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) {
            sendAmmoData(event.getPlayer());
            return;
        }

        loadData();
        event.getPlayerList().getPlayers().forEach(EventHandler::sendAmmoData);
    }

    private static void sendAmmoData(ServerPlayer player) {
        if (!player.connection.connection.isMemoryConnection()) {
            for (SyncAmmoDataPacket packet : ammoSyncPackets) {
                NetworkHandler.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), packet);
            }
        }
    }

    @SubscribeEvent
    public static void loadData(ServerStartedEvent event) {
        loadData();
    }

    @SubscribeEvent
    public static void clearAmmoSync(ServerStoppedEvent event) {
        ammoSyncPackets = List.of();
    }

    private static void loadData() {
        AmmoManager.clearJsonAmmoData();
        DataManager.clear(ModifierData.class);
        try {
            ModGson.INSTANCE.loadAll(FMLPaths.CONFIGDIR.get().resolve(TudMod.MODID).resolve("modifier_data"));
            ModGson.INSTANCE.loadAll(FMLPaths.CONFIGDIR.get().resolve(TudMod.MODID).resolve("gun_data"));
            ModGson.INSTANCE.loadAll(FMLPaths.CONFIGDIR.get().resolve(TudMod.MODID).resolve("ammo_data"));
        } catch (IOException e) {
            TudMod.LOGGER.error("Failed to load UniDict JSON data", e);
        }
        try {
            ammoSyncPackets = SyncAmmoDataPacket.createSnapshot();
        } catch (IOException | RuntimeException e) {
            ammoSyncPackets = List.of();
            TudMod.LOGGER.error("Failed to prepare UniDict ammo data sync", e);
        }
    }
}
