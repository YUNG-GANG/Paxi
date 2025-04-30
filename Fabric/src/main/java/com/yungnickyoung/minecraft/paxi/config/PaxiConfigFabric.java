package com.yungnickyoung.minecraft.paxi.config;

import com.yungnickyoung.minecraft.paxi.PaxiCommon;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name="paxi-fabric-" + PaxiCommon.VERSION_CONFIG_STR)
public class PaxiConfigFabric implements ConfigData {
    @ConfigEntry.Category("Paxi")
    @ConfigEntry.Gui.TransitiveObject
    public ConfigGeneralFabric general = new ConfigGeneralFabric();
}
