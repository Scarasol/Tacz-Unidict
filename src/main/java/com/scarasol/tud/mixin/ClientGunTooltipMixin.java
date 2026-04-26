package com.scarasol.tud.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.scarasol.tud.inventory.tooltip.CustomGunTooltip;
import com.scarasol.tud.manager.AmmoManager;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.client.tooltip.ClientGunTooltip;
import com.tacz.guns.inventory.tooltip.GunTooltip;
import com.tacz.guns.item.GunTooltipPart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * @author Scarasol
 */
@Mixin(ClientGunTooltip.class)
public abstract class ClientGunTooltipMixin implements ClientTooltipComponent {

    @Shadow @Final private ItemStack gun;
    @Shadow @Final private ItemStack ammo;

    @Shadow private @Nullable List<FormattedCharSequence> desc;
    @Shadow private Component ammoName;
    @Shadow private MutableComponent ammoCountText;
    @Shadow private @Nullable MutableComponent gunType;
    @Shadow private MutableComponent damage;
    @Shadow private MutableComponent armorIgnore;
    @Shadow private MutableComponent headShotMultiplier;
    @Shadow private MutableComponent weight;
    @Shadow private MutableComponent tips;
    @Shadow private MutableComponent levelInfo;
    @Shadow private @Nullable MutableComponent packInfo;

    @Shadow private int maxWidth;

    @Unique
    private boolean isItem;
    @Unique
    private boolean hideDamageInfo;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void tud$initFlags(GunTooltip tooltip, CallbackInfo ci) {
        if (tooltip instanceof CustomGunTooltip customGunTooltip) {
            this.isItem = customGunTooltip.isFlag();
        }

        com.scarasol.tud.data.AmmoData currentAmmoData = AmmoManager.getCurrentAmmoData(this.gun);
        if (currentAmmoData != null && currentAmmoData.getEntityId() != null) {
            this.hideDamageInfo = true;
        }

        if (this.hideDamageInfo) {
            tud$recalculateMaxWidth();
        }
    }

    @Inject(method = "getHeight", at = @At("HEAD"), cancellable = true)
    private void tud$getHeight(CallbackInfoReturnable<Integer> cir) {
        if (!hideDamageInfo) {
            return;
        }

        int height = 0;

        if (tud$shouldShow(GunTooltipPart.DESCRIPTION) && this.desc != null) {
            height += 10 * this.desc.size() + 2;
        }
        if (tud$shouldShow(GunTooltipPart.AMMO_INFO)) {
            height += 24;
        }
        if (tud$shouldShow(GunTooltipPart.BASE_INFO)) {
            height += 4;
            height += 10;
            if (this.gunType != null) {
                height += 10;
            }
        }
        if (tud$shouldShow(GunTooltipPart.EXTRA_DAMAGE_INFO)) {
            height += 4;
            height += 10;
        }
        if (tud$shouldShow(GunTooltipPart.UPGRADES_TIP)) {
            height += 14;
        }
        if (tud$shouldShow(GunTooltipPart.PACK_INFO) && this.packInfo != null) {
            height += 14;
        }

        cir.setReturnValue(height);
    }

    @Inject(method = "renderText", at = @At("HEAD"), cancellable = true)
    private void tud$renderText(Font font, int pX, int pY, Matrix4f matrix4f, MultiBufferSource.BufferSource bufferSource, CallbackInfo ci) {
        if (!hideDamageInfo) {
            return;
        }

        int yOffset = pY;

        if (tud$shouldShow(GunTooltipPart.DESCRIPTION) && this.desc != null) {
            yOffset += 2;
            for (FormattedCharSequence sequence : this.desc) {
                font.drawInBatch(sequence, pX, yOffset, 0xaaaaaa, false, matrix4f, bufferSource,
                        Font.DisplayMode.NORMAL, 0, 0xF000F0);
                yOffset += 10;
            }
        }

        if (tud$shouldShow(GunTooltipPart.AMMO_INFO)) {
            yOffset += 4;

            Component name = tud$getDisplayedAmmoName();
            font.drawInBatch(name, pX + 20, yOffset, 0xffaa00, false, matrix4f, bufferSource,
                    Font.DisplayMode.NORMAL, 0, 0xF000F0);

            font.drawInBatch(this.ammoCountText, pX + 20, yOffset + 10, 0x777777, false, matrix4f, bufferSource,
                    Font.DisplayMode.NORMAL, 0, 0xF000F0);

            yOffset += 20;
        }

        if (tud$shouldShow(GunTooltipPart.BASE_INFO)) {
            yOffset += 4;


            font.drawInBatch(this.levelInfo, pX, yOffset, 0x777777, false, matrix4f, bufferSource,
                    Font.DisplayMode.NORMAL, 0, 0xF000F0);
            yOffset += 10;

            if (this.gunType != null) {
                font.drawInBatch(this.gunType, pX, yOffset, 0x777777, false, matrix4f, bufferSource,
                        Font.DisplayMode.NORMAL, 0, 0xF000F0);
                yOffset += 10;
            }
        }

        if (tud$shouldShow(GunTooltipPart.EXTRA_DAMAGE_INFO)) {
            yOffset += 4;

            font.drawInBatch(this.weight, pX, yOffset, 0xffffff, false, matrix4f, bufferSource,
                    Font.DisplayMode.NORMAL, 0, 0xF000F0);
            yOffset += 10;
        }

        if (tud$shouldShow(GunTooltipPart.UPGRADES_TIP)) {
            yOffset += 4;
            font.drawInBatch(this.tips, pX, yOffset, 0xffffff, false, matrix4f, bufferSource,
                    Font.DisplayMode.NORMAL, 0, 0xF000F0);
            yOffset += 10;
        }

        if (tud$shouldShow(GunTooltipPart.PACK_INFO) && this.packInfo != null) {
            yOffset += 4;
            font.drawInBatch(this.packInfo, pX, yOffset, 0xffffff, false, matrix4f, bufferSource,
                    Font.DisplayMode.NORMAL, 0, 0xF000F0);
        }

        ci.cancel();
    }

    @WrapOperation(
            method = "renderImage",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/item/ItemStack;II)V")
    )
    private void tud$renderImage(GuiGraphics instance, ItemStack itemStack, int x, int y, Operation<Void> original) {
        if (isItem && itemStack.getItem() instanceof IAmmo ammoItem) {
            Item item = ForgeRegistries.ITEMS.getValue(ammoItem.getAmmoId(itemStack));
            if (item != null) {
                instance.renderItem(new ItemStack(item), x, y);
                return;
            }
        }
        original.call(instance, itemStack, x, y);
    }

    @WrapOperation(
            method = "renderText",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Font;drawInBatch(Lnet/minecraft/network/chat/Component;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I",
                    ordinal = 0
            )
    )
    private int tud$renderTextAmmoName(Font instance, Component text,
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

    @Unique
    private boolean tud$shouldShow(GunTooltipPart part) {
        return (GunTooltipPart.getHideFlags(this.gun) & part.getMask()) == 0;
    }

    @Unique
    private Component tud$getDisplayedAmmoName() {
        if (isItem && this.ammo.getItem() instanceof IAmmo ammoItem) {
            Item item = ForgeRegistries.ITEMS.getValue(ammoItem.getAmmoId(this.ammo));
            if (item != null) {
                return new ItemStack(item).getHoverName();
            }
        }
        return this.ammoName;
    }

    @Unique
    private void tud$recalculateMaxWidth() {
        Font font = Minecraft.getInstance().font;
        int w = 0;

        if (tud$shouldShow(GunTooltipPart.DESCRIPTION) && this.desc != null) {
            for (FormattedCharSequence sequence : this.desc) {
                w = Math.max(w, font.width(sequence));
            }
        }

        if (tud$shouldShow(GunTooltipPart.AMMO_INFO)) {
            w = Math.max(w, font.width(tud$getDisplayedAmmoName()) + 22);
            w = Math.max(w, font.width(this.ammoCountText) + 22);
        }

        if (tud$shouldShow(GunTooltipPart.BASE_INFO)) {
            w = Math.max(w, font.width(this.levelInfo));
            if (this.gunType != null) {
                w = Math.max(w, font.width(this.gunType));
            }
            if (!hideDamageInfo) {
                w = Math.max(w, font.width(this.damage));
            }
        }

        if (tud$shouldShow(GunTooltipPart.EXTRA_DAMAGE_INFO)) {
            if (!hideDamageInfo) {
                w = Math.max(w, font.width(this.armorIgnore));
                w = Math.max(w, font.width(this.headShotMultiplier));
            }
            w = Math.max(w, font.width(this.weight));
        }

        if (tud$shouldShow(GunTooltipPart.UPGRADES_TIP)) {
            w = Math.max(w, font.width(this.tips));
        }

        if (tud$shouldShow(GunTooltipPart.PACK_INFO) && this.packInfo != null) {
            w = Math.max(w, font.width(this.packInfo));
        }

        this.maxWidth = w;
    }
}
