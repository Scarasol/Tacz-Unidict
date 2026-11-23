package com.scarasol.tud.network;

import com.scarasol.tud.api.serialization.JsonData;
import com.scarasol.tud.client.network.ClientNetworkHandler;
import com.scarasol.tud.data.AmmoData;
import com.scarasol.tud.data.GunData;
import com.scarasol.tud.util.data.DataManager;
import com.scarasol.tud.util.io.ModGson;
import com.tacz.guns.resource.network.GunPackSync;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public record SyncAmmoDataPacket(GunPackSync.Part part) {
    public static List<SyncAmmoDataPacket> createSnapshot() throws IOException {
        List<JsonData> data = new ArrayList<>();
        data.addAll(DataManager.getModDataRegisterData(GunData.class));
        data.addAll(DataManager.getModDataRegisterData(AmmoData.class));
        // Replay oldest first to preserve the server's last-registration-wins lookup.
        Collections.reverse(data);
        String json = ModGson.INSTANCE.gson().toJson(data.toArray(JsonData[]::new));
        return GunPackSync.compressAndSplit(json.getBytes(StandardCharsets.UTF_8))
                .stream().map(SyncAmmoDataPacket::new).toList();
    }

    public static void encode(SyncAmmoDataPacket packet, FriendlyByteBuf buffer) {
        GunPackSync.Part part = packet.part();
        buffer.writeUUID(part.transferId());
        buffer.writeVarInt(part.index());
        buffer.writeVarInt(part.compressedSize());
        buffer.writeVarInt(part.uncompressedSize());
        buffer.writeByteArray(part.data());
    }

    public static SyncAmmoDataPacket decode(FriendlyByteBuf buffer) {
        return new SyncAmmoDataPacket(new GunPackSync.Part(buffer.readUUID(), buffer.readVarInt(),
                buffer.readVarInt(), buffer.readVarInt(), buffer.readByteArray(GunPackSync.CHUNK_SIZE)));
    }

    public static void handle(SyncAmmoDataPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientNetworkHandler.receiveAmmoData(packet.part(), context.getNetworkManager()));
        context.setPacketHandled(true);
    }
}
