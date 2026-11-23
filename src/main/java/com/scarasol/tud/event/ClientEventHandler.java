package com.scarasol.tud.event;

import com.scarasol.tud.TudMod;
import com.scarasol.tud.client.network.ClientNetworkHandler;
import com.scarasol.tud.client.screen.WheelMenuScreen;
import com.scarasol.tud.configuration.CommonConfig;
import com.scarasol.tud.data.GunData;
import com.scarasol.tud.init.TudKeyMappings;
import com.scarasol.tud.manager.AmmoManager;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.crafting.GunSmithTableRecipe;
import com.tacz.guns.resource.index.CommonGunIndex;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;


import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


/**
 * @author Scarasol
 */
@Mod.EventBusSubscriber(modid = TudMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientEventHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        if (!TudKeyMappings.WHEEL_KEY.matches(event.getKey(), event.getScanCode())) {
            return;
        }

        int action = event.getAction();

        if (action == GLFW.GLFW_PRESS) {
            ItemStack itemStack = mc.player.getMainHandItem();
            IGun iGun = IGun.getIGunOrNull(itemStack);
            if (iGun != null) {
                ResourceLocation gunId = iGun.getGunId(itemStack);
                Optional<CommonGunIndex> commonGunIndex = TimelessAPI.getCommonGunIndex(gunId);

                String ammoIdStr = commonGunIndex.map((gunIndex) ->
                        gunIndex.getGunData().getAmmoId().toString()).orElse("");
                if (AmmoManager.canUseGeneralAmmo(gunId.toString(), ammoIdStr)) {
                    GunData gunData = AmmoManager.getGunData(gunId);
                    if (gunData != null) {
                        WheelMenuScreen.setWheelItems(gunData.getAllAmmo());
                        if (mc.screen == null && WheelMenuScreen.hasWheelItems()) {
                            mc.setScreen(new WheelMenuScreen());
                        }
                    }
                }
            }
            return;
        }

        if (action == GLFW.GLFW_RELEASE) {
            if (mc.screen instanceof WheelMenuScreen) {
                mc.screen.onClose();
                mc.setScreen(null);
            }
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        if (event.getConnection() != null && !event.getConnection().isMemoryConnection()) {
            ClientNetworkHandler.clearAmmoSync();
        }
    }

    @SubscribeEvent
    public static void recipeCancel(RecipesUpdatedEvent event) {
        if (!CommonConfig.RICIPE_REMOVE.get()) {
            return;
        }

        RecipeManager manager = event.getRecipeManager();

        @SuppressWarnings("unchecked")
        RecipeType<GunSmithTableRecipe> type =
                (RecipeType<GunSmithTableRecipe>) net.minecraft.core.registries.BuiltInRegistries.RECIPE_TYPE.get(
                        new ResourceLocation("tacz", "gun_smith_table_crafting")
                );

        if (type == null) {
            return;
        }
        List<GunSmithTableRecipe> recipes = manager.getAllRecipesFor(type);
        TagKey<Item> removeTag = TagKey.create(net.minecraft.core.registries.Registries.ITEM, new ResourceLocation("tacz:recipe_remove"));
        Set<Recipe<?>> removedRecipe = new HashSet<>();
        for (Recipe<?> recipe : recipes) {
            if (recipe instanceof GunSmithTableRecipe gunSmithTableRecipe) {
                if (gunSmithTableRecipe.getOutput().is(removeTag)) {
                    removedRecipe.add(recipe);
                }
            }
        }
        List<Recipe<?>> newRecipes = manager.getRecipes().stream()
                .filter(recipe -> !removedRecipe.contains(recipe)).toList();
        manager.replaceRecipes(newRecipes);
    }

}
