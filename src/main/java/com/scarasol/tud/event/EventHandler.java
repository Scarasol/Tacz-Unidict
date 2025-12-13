package com.scarasol.tud.event;

import com.scarasol.tud.configuration.CommonConfig;
import com.tacz.guns.crafting.GunSmithTableRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Scarasol
 */
@Mod.EventBusSubscriber
public class EventHandler {
    @SubscribeEvent
    public static void recipeCancel(RecipesUpdatedEvent event) {
        if (!CommonConfig.RICIPE_REMOVE.get()) {
            return;
        }

        RecipeManager manager = event.getRecipeManager();

        @SuppressWarnings("unchecked")
        RecipeType<GunSmithTableRecipe> type =
                (RecipeType<GunSmithTableRecipe>) BuiltInRegistries.RECIPE_TYPE.get(
                        new ResourceLocation("tacz", "gun_smith_table_crafting")
                );

        if (type == null) {
            return;
        }
        List<GunSmithTableRecipe> recipes = manager.getAllRecipesFor(type);
        TagKey<Item> removeTag = TagKey.create(Registries.ITEM, new ResourceLocation("tacz:recipe_remove"));
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
