package com.yungnickyoung.minecraft.paxi;

import net.minecraft.server.packs.PackType;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.nio.file.Paths;

@Mod(PaxiCommon.MOD_ID)
public class PaxiForge {
    public PaxiForge() {
        PaxiCommon.BASE_PACK_DIRECTORY = new File(FMLPaths.CONFIGDIR.get().toString(), "paxi");
        PaxiCommon.DATA_PACK_DIRECTORY = Paths.get(PaxiCommon.BASE_PACK_DIRECTORY.toString(), "datapacks");
        PaxiCommon.RESOURCE_PACK_DIRECTORY = Paths.get(PaxiCommon.BASE_PACK_DIRECTORY.toString(), "resourcepacks");
        PaxiCommon.DATAPACK_ORDERING_FILE = new File(PaxiCommon.BASE_PACK_DIRECTORY, "datapack_load_order.json");
        PaxiCommon.RESOURCEPACK_ORDERING_FILE = new File(PaxiCommon.BASE_PACK_DIRECTORY, "resourcepack_load_order.json");
        PaxiCommon.init();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(PaxiForge::addPaxiPackSource);
    }

    private static void addPaxiPackSource(AddPackFindersEvent event) {
        switch (event.getPackType()) {
            case CLIENT_RESOURCES -> event.addRepositorySource(new PaxiRepositorySource(PaxiCommon.RESOURCE_PACK_DIRECTORY, PackType.CLIENT_RESOURCES, PaxiCommon.RESOURCEPACK_ORDERING_FILE));
            case SERVER_DATA -> event.addRepositorySource(new PaxiRepositorySource(PaxiCommon.DATA_PACK_DIRECTORY, PackType.SERVER_DATA, PaxiCommon.DATAPACK_ORDERING_FILE));
        }
    }
}
