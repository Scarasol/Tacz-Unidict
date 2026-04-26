package com.scarasol.tud.api.functional;

import com.scarasol.tud.api.data.ModData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;


/**
 * @author Scarasol
 */
@FunctionalInterface
public interface AmmoGetter extends ModData {
    Tuple<ResourceLocation, Boolean> getCurrentAmmo(ItemStack itemStack);
}
