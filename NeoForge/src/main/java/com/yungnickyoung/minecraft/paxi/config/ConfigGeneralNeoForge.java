package com.yungnickyoung.minecraft.paxi.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ConfigGeneralNeoForge {
    public final ModConfigSpec.ConfigValue<Boolean> loadFromBaseDatapacksDirectory;

    public ConfigGeneralNeoForge(final ModConfigSpec.Builder BUILDER) {
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
                .gameRestart()
                .define("Load from base 'datapacks' directory", true);

        BUILDER.pop();
    }
}
