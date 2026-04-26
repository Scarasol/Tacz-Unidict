package com.scarasol.tud;

import com.mojang.logging.LogUtils;
import com.scarasol.tud.configuration.CommonConfig;
import com.scarasol.tud.init.TudEntities;
import com.scarasol.tud.init.TudModData;
import com.scarasol.tud.manager.EntitySpawnManager;
import com.scarasol.tud.network.NetworkHandler;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * @author Scarasol
 */
@Mod(TudMod.MODID)
public class TudMod
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "tacz_unidict";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public TudMod()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC, "tacz-unidict-common.toml");
        modEventBus.addListener(this::commonSetup);
        TudEntities.REGISTRY.register(modEventBus);
        NetworkHandler.addNetworkMessage();
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        TudModData.registerType();
        TudModData.registerAmmoGetter();
        TudModData.registerModifierGetter();
        EntitySpawnManager.registerEntityGetter();
    }


}
