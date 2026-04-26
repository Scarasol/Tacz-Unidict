package com.scarasol.tud.network;

import com.scarasol.tud.client.network.ClientNetworkHandler;
import com.scarasol.tud.manager.AmmoManager;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public record SwitchAmmoPacket(int id) {

    public static SwitchAmmoPacket decode(FriendlyByteBuf buf) {

        return new SwitchAmmoPacket(buf.readInt());
    }

    public static void encode(SwitchAmmoPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.id());
    }

    public static void handler(SwitchAmmoPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (msg != null) {

                if (context.get().getDirection().getReceptionSide().isServer()) {
                    ServerPlayer serverPlayer = context.get().getSender();
                    if (serverPlayer != null) {
                        ItemStack itemStack = serverPlayer.getMainHandItem();
                        IGun iGun = IGun.getIGunOrNull(itemStack);
                        if (iGun != null) {
                            if (serverPlayer.isCreative()) {
                                itemStack.getOrCreateTag().putInt("TudCurrentAmmo", msg.id());
                            }
                            iGun.dropAllAmmo(serverPlayer, itemStack);
                            if (iGun.hasBulletInBarrel(itemStack) && !serverPlayer.isCreative()) {
                                iGun.setBulletInBarrel(itemStack, false);
                                ItemStack ammoItem = AmmoManager.getGunAmmo(itemStack);
                                ItemHandlerHelper.giveItemToPlayer(serverPlayer, ammoItem);
                            }
                            itemStack.getOrCreateTag().putInt("TudCurrentAmmo", msg.id());
                            IGunOperator.fromLivingEntity(serverPlayer).initialData();
                            NetworkHandler.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> serverPlayer), msg);
//                            AttachmentPropertyManager.postChangeEvent(serverPlayer, itemStack);
                        }
                    }
                }else {
                    ClientNetworkHandler.reload();
                }
            }

        });
        context.get().setPacketHandled(true);
    }
}
