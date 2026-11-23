package com.scarasol.tud.client.network;

import com.google.gson.JsonParseException;
import com.scarasol.tud.TudMod;
import com.scarasol.tud.api.serialization.JsonData;
import com.scarasol.tud.data.AmmoData;
import com.scarasol.tud.data.GunData;
import com.scarasol.tud.manager.AmmoManager;
import com.scarasol.tud.util.io.ModGson;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.gui.overlay.GunHudOverlay;
import com.tacz.guns.resource.network.GunPackSync;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Scarasol
 */
public class ClientNetworkHandler {
    private static final GunPackSync.Receiver AMMO_SYNC = new GunPackSync.Receiver();

    public static void receiveAmmoData(GunPackSync.Part part, Connection connection) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientPacketListener listener = minecraft.getConnection();
        if (listener == null || listener.getConnection() != connection || !connection.isConnected()
                || connection.isMemoryConnection()) {
            return;
        }

        try {
            byte[] data = AMMO_SYNC.accept(part);
            if (data == null) {
                return;
            }
            JsonData[] snapshot = ModGson.INSTANCE.gson().fromJson(new String(data, StandardCharsets.UTF_8), JsonData[].class);
            if (snapshot == null) {
                throw new JsonParseException("Missing UniDict ammo data snapshot");
            }
            for (JsonData entry : snapshot) {
                if (!(entry instanceof GunData gun && gun.gunId() != null)
                        && !(entry instanceof AmmoData ammo && ammo.getAmmoId() != null)) {
                    throw new JsonParseException("Invalid UniDict ammo data entry");
                }
            }

            AmmoManager.clearJsonAmmoData();
            for (JsonData entry : snapshot) {
                entry.onLoaded();
            }
            if (minecraft.player != null) {
                IClientPlayerGunOperator.fromLocalPlayer(minecraft.player).getDataHolder().resetAmmoCountCache();
            }
            GunHudOverlay.resetTimestamp();
        } catch (IOException | RuntimeException e) {
            AMMO_SYNC.clear();
            TudMod.LOGGER.error("Failed to synchronize UniDict ammo data", e);
            connection.disconnect(Component.literal("Failed to synchronize UniDict ammo data"));
        }
    }

    public static void clearAmmoSync() {
        AMMO_SYNC.clear();
        AmmoManager.clearJsonAmmoData();
    }

    public static void reload() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.isSpectator()) {
            return;
        }
        if (player.getMainHandItem().getItem() instanceof IGun iGun) {
            if (iGun.useInventoryAmmo(player.getMainHandItem())) {
                return;
            }
            IClientPlayerGunOperator.fromLocalPlayer(player).reload();
        }
    }
}
