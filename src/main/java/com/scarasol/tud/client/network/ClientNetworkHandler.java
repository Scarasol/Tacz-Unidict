package com.scarasol.tud.client.network;

import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.item.IGun;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * @author Scarasol
 */
public class ClientNetworkHandler {
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
