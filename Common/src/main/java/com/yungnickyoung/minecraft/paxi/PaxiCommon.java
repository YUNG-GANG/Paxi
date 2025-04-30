package com.yungnickyoung.minecraft.paxi;

import com.yungnickyoung.minecraft.paxi.module.ConfigModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;

public class PaxiCommon {
    public static final String MOD_ID = "paxi";
    public static final String VERSION_CONFIG_STR = "1_21"; // TODO: Update this for each release

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final ConfigModule CONFIG = new ConfigModule();

    public static File BASE_GAME_DIRECTORY;
    public static File BASE_PACK_DIRECTORY;
    public static Path DATA_PACK_DIRECTORY;
    public static Path RESOURCE_PACK_DIRECTORY;
    public static File DATAPACK_ORDERING_FILE;
    public static File RESOURCEPACK_ORDERING_FILE;

    public static void init() {
    }
}
