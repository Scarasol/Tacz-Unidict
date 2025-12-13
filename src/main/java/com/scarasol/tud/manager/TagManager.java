package com.scarasol.tud.manager;

import com.scarasol.tud.configuration.CommonConfig;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.api.item.builder.AttachmentItemBuilder;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.init.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Scarasol
 */
public class TagManager {

    public static final Map<TagKey<Item>, ResourceLocation> TAG_REPLACE = new HashMap<>();
    private static boolean INIT;

    public static void init() {
        CommonConfig.ITEM_REPLACE.get().forEach(str -> {
            String[] info = str.split(",");
            if (info.length < 2) {
                return;
            }
            TagKey<Item> tag = TagKey.create(Registries.ITEM, new ResourceLocation(info[0].toLowerCase().trim()));
            ResourceLocation resourceLocation = new ResourceLocation(info[1].toLowerCase().trim());
            TAG_REPLACE.put(tag, resourceLocation);
        });
    }

    public static ItemStack getItemStackForReplace(ItemStack itemStack) {
        if (!INIT) {
            init();
            INIT = true;
        }
        return TAG_REPLACE.entrySet().stream()
                .filter(entry -> itemStack.is(entry.getKey()))
                .map(entry -> getItemStack(entry.getValue(), itemStack.getCount()))
                .findFirst()
                .orElse(itemStack);
    }

    public static ItemStack getItemStack(ResourceLocation resourceLocation, int count) {
        ItemStack itemStack;
        if (TimelessAPI.getCommonGunIndex(resourceLocation).isPresent()) {
            itemStack = GunItemBuilder.create().setId(resourceLocation).setCount(count).build();
        } else if (TimelessAPI.getCommonAmmoIndex(resourceLocation).isPresent()) {
            itemStack = AmmoItemBuilder.create().setId(resourceLocation).setCount(count).build();
        } else if (TimelessAPI.getCommonAttachmentIndex(resourceLocation).isPresent()) {
            itemStack = AttachmentItemBuilder.create().setId(resourceLocation).setCount(count).build();
        } else {
            itemStack = new ItemStack(ForgeRegistries.ITEMS.getValue(resourceLocation));
        }
        return itemStack;
    }
}
