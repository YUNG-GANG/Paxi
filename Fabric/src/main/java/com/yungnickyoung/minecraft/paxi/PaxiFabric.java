package com.yungnickyoung.minecraft.paxi;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;

public class PaxiFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        PaxiCommon.BASE_PACK_DIRECTORY =  new File(FabricLoader.getInstance().getConfigDir().toString(), "paxi");
        PaxiCommon.DATA_PACK_DIRECTORY = new File(PaxiCommon.BASE_PACK_DIRECTORY, "datapacks");
        PaxiCommon.RESOURCE_PACK_DIRECTORY = new File(PaxiCommon.BASE_PACK_DIRECTORY, "resourcepacks");
        PaxiCommon.DATAPACK_ORDERING_FILE = new File(PaxiCommon.BASE_PACK_DIRECTORY, "datapack_load_order.json");
        PaxiCommon.init();
    }
}
