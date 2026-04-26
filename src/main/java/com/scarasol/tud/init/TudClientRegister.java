package com.scarasol.tud.init;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.scarasol.tud.TudMod;
import com.scarasol.tud.inventory.tooltip.CustomGunTooltip;
import com.tacz.guns.client.tooltip.ClientGunTooltip;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

import static com.scarasol.tud.init.TudKeyMappings.WHEEL_KEY;

/**
 * @author Scarasol
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT, modid = TudMod.MODID)
public class TudClientRegister {

    @SubscribeEvent
    public static void onClientSetup(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(CustomGunTooltip.class, ClientGunTooltip::new);
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(WHEEL_KEY);
    }

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        new ResourceLocation(TudMod.MODID, "wheel_menu"),
                        DefaultVertexFormat.POSITION_TEX
                ),
                shader -> TudShaders.WHEEL_MENU_SHADER = shader
        );
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(TudEntities.SHOT_FALLING_BLOCK.get(), FallingBlockRenderer::new);
    }
}
