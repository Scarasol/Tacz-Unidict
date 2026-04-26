package com.scarasol.tud.init;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;


/**
 * @author Scarasol
 */
public class TudKeyMappings {

    public static final KeyMapping WHEEL_KEY = new KeyMapping(
            "key.tacz_unidict.wheel_menu",
            GLFW.GLFW_KEY_C,
            "key.categories.tacz_unidict"
    );

}
