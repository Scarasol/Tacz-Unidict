package com.scarasol.tud.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.scarasol.tud.inventory.tooltip.CustomGunTooltip;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.client.tooltip.ClientGunTooltip;
import com.tacz.guns.inventory.tooltip.GunTooltip;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Scarasol
 */
@Mixin(ClientGunTooltip.class)
public abstract class ClientGunTooltipMixin implements ClientTooltipComponent {

    @Shadow @Final private ItemStack ammo;
    @Unique
    private boolean isItem;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void tud$isItem(GunTooltip tooltip, CallbackInfo ci) {
        if (tooltip instanceof CustomGunTooltip customGunTooltip) {
            isItem = customGunTooltip.isFlag();
        }
    }

    @WrapOperation(method = "renderImage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/item/ItemStack;II)V"))
    private void tud$renderImage(GuiGraphics instance, ItemStack itemStack, int x, int y, Operation<Void> original) {
        if (isItem && itemStack.getItem() instanceof IAmmo ammo) {
            Item item = ForgeRegistries.ITEMS.getValue(ammo.getAmmoId(itemStack));
            if (item != null) {
                instance.renderItem(new ItemStack(item), x, y);
                return;
            }
        }
        original.call(instance, itemStack, x, y);
    }

    @WrapOperation(method = "renderText", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawInBatch(Lnet/minecraft/network/chat/Component;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I", ordinal = 0))
    private int tud$renderText(Font instance, Component text,
                            float x, float y,
                            int color,
                            boolean shadow,
                            Matrix4f poseMatrix,
                            MultiBufferSource bufferSource,
                            Font.DisplayMode displayMode,
                            int backgroundColor,
                            int packedLight,
                            Operation<Integer> original) {
        if (isItem && ammo.getItem() instanceof IAmmo ammoItem) {
            Item item = ForgeRegistries.ITEMS.getValue(ammoItem.getAmmoId(ammo));
            if (item != null) {
                return instance.drawInBatch(new ItemStack(item).getHoverName(), x, y, color, shadow, poseMatrix, bufferSource, displayMode, backgroundColor, packedLight);
            }
        }
        return original.call(instance, text, x, y, color, shadow, poseMatrix, bufferSource, displayMode, backgroundColor, packedLight);
    }
}
