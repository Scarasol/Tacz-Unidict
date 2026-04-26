package com.scarasol.tud.compat;

import com.scarasol.tageditor.compat.tacz.TaczTagHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

/**
 * @author Scarasol
 */
public class TagEditorCompat {

    public static Set<ResourceLocation> getAllTags(ItemStack itemStack) {
        return TaczTagHelper.getAllItemTags(itemStack);
    }

    public static Set<ResourceLocation> getAllTags(ResourceLocation gunId) {
        return TaczTagHelper.getAllItemTags(gunId);
    }
}
