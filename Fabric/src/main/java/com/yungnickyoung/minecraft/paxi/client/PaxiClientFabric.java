package com.yungnickyoung.minecraft.paxi.client;

import net.fabricmc.api.ClientModInitializer;

public class PaxiClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PaxiClientCommon.init();
    }
}
