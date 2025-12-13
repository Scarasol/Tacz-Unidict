package com.scarasol.tud.configuration;

import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

/**
 * @author Scarasol
 */
public class CommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> TYPE_TO_AMMO;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> GUN_WHITELIST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> AMMO_WHITELIST;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ITEM_REPLACE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> RICIPE_REMOVE;

    static {
        TYPE_TO_AMMO = BUILDER.comment("""
                Which ammo is used by each type of gun.
                Reload type supported.
                Format: "pistol, tacz:9mm" — this means that all guns of the type pistol will use the 9mm.
                "pistol, $minecraft:gold_nugget" - use $ to make the guns use item as ammo.
                "#forge:gun, tacz:9mm" - When the Tag Editor mod is installed, you can use tags to categorize firearms.
                """)
                .defineList("Ammo of Gun",
                        Lists.newArrayList("pistol, tacz_unidict:pistol", "sniper, tacz_unidict:sniper", "rifle, tacz_unidict:rifle", "shotgun, tacz_unidict:shot", "smg, tacz_unidict:pistol", "rpg, tacz_unidict:barrel", "mg, tacz_unidict:rifle", "fuel, tacz_unidict:fuel_tank"),
                        (element) -> true);
        GUN_WHITELIST = BUILDER.comment("Which guns are not affected by this mod.")
                .defineList("Gun WhiteList", Lists.newArrayList(), (element) -> true);
        AMMO_WHITELIST = BUILDER.comment("Guns that use these types of ammo will not be affected by this mod.")
                .defineList("Ammo WhiteList", Lists.newArrayList(), (element) -> true);

        BUILDER.push("Compat: Tag Editor");
        ITEM_REPLACE = BUILDER.comment("""
                Replace TACZ items in loot tables.
                Format: forge:need_to_replace, minecraft:diamond — this means all items with the tag forge:need_to_replace will be replaced with diamonds.
                You can also use gunID, ammoID, or attachmentID directly.""")
                .defineList("LootTable Replace", Lists.newArrayList(), (element) -> true);
        RICIPE_REMOVE = BUILDER.comment("Should all TACZ items with the tag tacz:recipe_remove have their recipes removed? (Only effective for TACZ’s default crafting recipes)")
                .define("Recipe Remove", true);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
