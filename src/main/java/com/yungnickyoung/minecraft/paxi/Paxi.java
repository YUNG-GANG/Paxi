package com.yungnickyoung.minecraft.paxi;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class Paxi implements ModInitializer {
    public static final String MOD_ID = "paxi";
    public static final File PACK_DIRECTORY = new File(FabricLoader.getInstance().getConfigDir().toString(), "paxi");
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Create global datapack directory if doesn't already exist
        try {
            String filePath = PACK_DIRECTORY.getCanonicalPath();
            if (PACK_DIRECTORY.mkdirs()) {
                LOGGER.info("Creating directory for global datapacks at " + filePath);
            }
        } catch (IOException e) {
            LOGGER.error("ERROR creating directory for global datapacks: {}", e.toString());
        }
    }
}