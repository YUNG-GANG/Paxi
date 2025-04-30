package com.yungnickyoung.minecraft.paxi.module;

import com.yungnickyoung.minecraft.paxi.PaxiCommon;
import com.yungnickyoung.minecraft.paxi.config.PaxiConfigForge;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ConfigModuleForge {
    public static void init() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, PaxiConfigForge.SPEC, "paxi-forge-"
                + PaxiCommon.VERSION_CONFIG_STR + ".toml");
        MinecraftForge.EVENT_BUS.addListener(ConfigModuleForge::onWorldLoad);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ConfigModuleForge::onConfigChange);
    }

    private static void onWorldLoad(LevelEvent.Load event) {
        bakeConfig();
    }

    private static void onConfigChange(ModConfigEvent event) {
        if (event.getConfig().getSpec() == PaxiConfigForge.SPEC) {
            bakeConfig();
        }
    }

    private static void bakeConfig() {
        PaxiCommon.CONFIG.loadFromBaseDatapacksDirectory = PaxiConfigForge.general.loadFromBaseDatapacksDirectory.get();
    }
}