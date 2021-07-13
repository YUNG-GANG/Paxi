package com.yungnickyoung.minecraft.paxi;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class Paxi {
    public static final String MOD_ID = "paxi";
    public static final File BASE_PACK_DIRECTORY = new File(FabricLoader.getInstance().getConfigDir().toString(), "paxi");
    public static final File DATA_PACK_DIRECTORY = new File(BASE_PACK_DIRECTORY, "datapacks");
    public static final File RESOURCE_PACK_DIRECTORY = new File(BASE_PACK_DIRECTORY, "resourcepacks");
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
}
