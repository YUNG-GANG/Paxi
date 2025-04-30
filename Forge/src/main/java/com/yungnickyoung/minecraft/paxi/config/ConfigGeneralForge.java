package com.yungnickyoung.minecraft.paxi.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigGeneralForge {
    public final ForgeConfigSpec.ConfigValue<Boolean> loadFromBaseDatapacksDirectory;

    public ConfigGeneralForge(final ForgeConfigSpec.Builder BUILDER) {
        BUILDER
                .comment(
                        """
                                ##########################################################################################################
                                # General settings.
                                ##########################################################################################################""")
                .push("General");

        loadFromBaseDatapacksDirectory = BUILDER
                .comment(
                        """
                                Whether Paxi should also force-load all data packs placed in the 'datapacks' folder in the base Minecraft folder.
                                This is for compatibility with CurseForge's new data pack system.
                                Default: true""".indent(1))
                .worldRestart()
                .define("Load from base 'datapacks' directory", true);

        BUILDER.pop();
    }
}
