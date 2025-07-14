package com.scarasol.tud.init;

import com.scarasol.tud.TudMod;
import com.scarasol.tud.inventory.tooltip.CustomGunTooltip;
import com.tacz.guns.client.tooltip.ClientGunTooltip;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author Scarasol
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT, modid = TudMod.MODID)
public class TudClientRegister {

    @SubscribeEvent
    public static void onClientSetup(RegisterClientTooltipComponentFactoriesEvent event) {
        // 注册文本提示
        event.register(CustomGunTooltip.class, ClientGunTooltip::new);
    }
}
