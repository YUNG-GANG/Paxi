package com.yungnickyoung.minecraft.paxi.module;

import com.yungnickyoung.minecraft.paxi.PaxiCommon;
import com.yungnickyoung.minecraft.paxi.config.PaxiConfigFabric;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraft.world.InteractionResult;

public class ConfigModuleFabric {
    public static void init() {
        AutoConfig.register(PaxiConfigFabric.class, Toml4jConfigSerializer::new);
        AutoConfig.getConfigHolder(PaxiConfigFabric.class).registerSaveListener(ConfigModuleFabric::bakeConfig);
        AutoConfig.getConfigHolder(PaxiConfigFabric.class).registerLoadListener(ConfigModuleFabric::bakeConfig);
        bakeConfig(AutoConfig.getConfigHolder(PaxiConfigFabric.class).get());
    }

    private static InteractionResult bakeConfig(ConfigHolder<PaxiConfigFabric> configHolder, PaxiConfigFabric configFabric) {
        bakeConfig(configFabric);
        return InteractionResult.SUCCESS;
    }

    private static void bakeConfig(PaxiConfigFabric configFabric) {
        PaxiCommon.CONFIG.loadFromBaseDatapacksDirectory = configFabric.general.loadFromBaseDatapacksDirectory;
    }
}
