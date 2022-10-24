package com.yungnickyoung.minecraft.paxi;

import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;

@Mod(PaxiCommon.MOD_ID)
public class PaxiForge {
    public PaxiForge() {
        PaxiCommon.BASE_PACK_DIRECTORY = new File(FMLPaths.CONFIGDIR.get().toString(), "paxi");
        PaxiCommon.DATA_PACK_DIRECTORY = new File(PaxiCommon.BASE_PACK_DIRECTORY, "datapacks");
        PaxiCommon.RESOURCE_PACK_DIRECTORY = new File(PaxiCommon.BASE_PACK_DIRECTORY, "resourcepacks");
        PaxiCommon.DATAPACK_ORDERING_FILE = new File(PaxiCommon.BASE_PACK_DIRECTORY, "datapack_load_order.json");
        PaxiCommon.init();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(PaxiForge::addPaxiPackSource);
    }

    private static void addPaxiPackSource(AddPackFindersEvent event) {
        switch (event.getPackType()) {
            case CLIENT_RESOURCES -> event.addRepositorySource(new PaxiRepositorySource(PaxiCommon.RESOURCE_PACK_DIRECTORY));
            case SERVER_DATA -> event.addRepositorySource(new PaxiRepositorySource(PaxiCommon.DATA_PACK_DIRECTORY, PaxiCommon.DATAPACK_ORDERING_FILE));
        }
    }
}
