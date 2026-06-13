package com.scarasol.tud.mixin;

import com.scarasol.tud.data.AmmoData;
import com.scarasol.tud.manager.AmmoManager;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.sound.GunSoundInstance;
import com.tacz.guns.config.common.GunConfig;
import com.tacz.guns.init.ModSounds;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.sound.SoundManager;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(targets = "com.tacz.guns.client.sound.SoundPlayManager")
public class SoundPlayManagerMixin {

    @Inject(method = "playShootSound", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onPlayShootSound(LivingEntity entity, GunDisplayInstance gunIndex, GunData gunData, CallbackInfo ci) {
        ItemStack gunItem = getGunItem(entity);
        AmmoData ammoData = AmmoManager.getCurrentAmmoData(gunItem);
        if (ammoData != null) {
            float volume = 0.8f;
            float pitchBase = 0.9f;
            float pitchRandom = 0.125f;
            int distance = (int) (GunConfig.DEFAULT_GUN_FIRE_SOUND_DISTANCE.get() * gunData.getFireSound().getFireMultiplier());

            if (ammoData.getSoundVolume() != null) volume = ammoData.getSoundVolume();
            if (ammoData.getSoundPitch() != null) {
                pitchBase = 0.9f * ammoData.getSoundPitch();
                pitchRandom = 0.125f * ammoData.getSoundPitch();
            }
            if (ammoData.getSoundDistanceMultiplier() != null) {
                distance = Math.round(distance * ammoData.getSoundDistanceMultiplier());
                if (distance < 0) distance = 0;
            }

            float pitch = pitchBase + entity.getRandom().nextFloat() * pitchRandom;
            GunSoundInstance instance = new GunSoundInstance(
                    ModSounds.GUN.get(),
                    SoundSource.PLAYERS,
                    volume,
                    pitch,
                    entity,
                    distance,
                    gunIndex.getSounds(SoundManager.SHOOT_SOUND),
                    false
            );
            Minecraft.getInstance().getSoundManager().play(instance);
            ci.cancel();
        }
    }

    @Inject(method = "playSilenceSound", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onPlaySilenceSound(LivingEntity entity, GunDisplayInstance gunIndex, GunData gunData, CallbackInfo ci) {
        ItemStack gunItem = getGunItem(entity);
        AmmoData ammoData = AmmoManager.getCurrentAmmoData(gunItem);
        if (ammoData != null) {
            float volume = 0.6f;
            float pitchBase = 0.9f;
            float pitchRandom = 0.125f;
            int distance = (int) (GunConfig.DEFAULT_GUN_SILENCE_SOUND_DISTANCE.get() * gunData.getFireSound().getSilenceMultiplier());

            if (ammoData.getSoundVolume() != null) volume = ammoData.getSoundVolume();
            if (ammoData.getSoundPitch() != null) {
                pitchBase = 0.9f * ammoData.getSoundPitch();
                pitchRandom = 0.125f * ammoData.getSoundPitch();
            }
            if (ammoData.getSoundDistanceMultiplier() != null) {
                distance = Math.round(distance * ammoData.getSoundDistanceMultiplier());
                if (distance < 0) distance = 0;
            }

            float pitch = pitchBase + entity.getRandom().nextFloat() * pitchRandom;
            GunSoundInstance instance = new GunSoundInstance(
                    ModSounds.GUN.get(),
                    SoundSource.PLAYERS,
                    volume,
                    pitch,
                    entity,
                    distance,
                    gunIndex.getSounds(SoundManager.SILENCE_SOUND),
                    false
            );
            Minecraft.getInstance().getSoundManager().play(instance);
            ci.cancel();
        }
    }

    private static ItemStack getGunItem(LivingEntity entity) {
        if (entity instanceof net.minecraft.client.player.LocalPlayer player) {
            return player.getMainHandItem();
        }
        return ItemStack.EMPTY;
    }
}