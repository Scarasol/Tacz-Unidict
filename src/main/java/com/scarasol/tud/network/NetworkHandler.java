package com.scarasol.tud.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

import static com.scarasol.tud.TudMod.MODID;

/**
 * @author Scarasol
 */
public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "2";
    public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID, MODID), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    private static int messageID = 0;

    public static <T> void addNetworkMessage() {
        PACKET_HANDLER.registerMessage(messageID++, SwitchAmmoPacket.class, SwitchAmmoPacket::encode, SwitchAmmoPacket::decode, SwitchAmmoPacket::handler);
        PACKET_HANDLER.registerMessage(messageID++, SyncAmmoDataPacket.class, SyncAmmoDataPacket::encode,
                SyncAmmoDataPacket::decode, SyncAmmoDataPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
