package com.yungnickyoung.minecraft.paxi;

import net.minecraft.server.packs.PackType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.io.File;
import java.nio.file.Paths;

@Mod(PaxiCommon.MOD_ID)
public class PaxiNeoForge {
    public PaxiNeoForge(IEventBus eventBus) {
        PaxiCommon.BASE_PACK_DIRECTORY = new File(FMLPaths.CONFIGDIR.get().toString(), "paxi");
        PaxiCommon.DATA_PACK_DIRECTORY = Paths.get(PaxiCommon.BASE_PACK_DIRECTORY.toString(), "datapacks");
        PaxiCommon.RESOURCE_PACK_DIRECTORY = Paths.get(PaxiCommon.BASE_PACK_DIRECTORY.toString(), "resourcepacks");
        PaxiCommon.DATAPACK_ORDERING_FILE = new File(PaxiCommon.BASE_PACK_DIRECTORY, "datapack_load_order.json");
        PaxiCommon.RESOURCEPACK_ORDERING_FILE = new File(PaxiCommon.BASE_PACK_DIRECTORY, "resourcepack_load_order.json");
        PaxiCommon.init();
        eventBus.addListener(PaxiNeoForge::addPaxiPackSource);
    }

    private static void addPaxiPackSource(AddPackFindersEvent event) {
        switch (event.getPackType()) {
            case CLIENT_RESOURCES -> event.addRepositorySource(new PaxiRepositorySource(PaxiCommon.RESOURCE_PACK_DIRECTORY, PackType.CLIENT_RESOURCES, PaxiCommon.RESOURCEPACK_ORDERING_FILE));
            case SERVER_DATA -> event.addRepositorySource(new PaxiRepositorySource(PaxiCommon.DATA_PACK_DIRECTORY, PackType.SERVER_DATA, PaxiCommon.DATAPACK_ORDERING_FILE));
        }
    }

}
