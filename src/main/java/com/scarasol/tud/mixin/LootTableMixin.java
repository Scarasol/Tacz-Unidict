package com.scarasol.tud.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.scarasol.tud.manager.TagManager;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;


import java.util.List;

/**
 * @author Scarasol
 */
@Mixin(LootTable.class)
public abstract class LootTableMixin {

    @WrapOperation(method = "fill", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Container;setItem(ILnet/minecraft/world/item/ItemStack;)V"))
    private void tud$onFill(Container instance, int i, ItemStack itemStack, Operation<Void> operation){
        if ("tacz".equals(ForgeRegistries.ITEMS.getKey(itemStack.getItem()).getNamespace())) {
            operation.call(instance, i, TagManager.getItemStackForReplace(itemStack));
        }else {
            operation.call(instance, i, itemStack);
        }
    }
}
