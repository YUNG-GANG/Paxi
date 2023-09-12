package com.yungnickyoung.minecraft.paxi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;

public class PaxiCommon {
    public static final String MOD_ID = "paxi";
    public static File BASE_PACK_DIRECTORY;
    public static Path DATA_PACK_DIRECTORY;
    public static Path RESOURCE_PACK_DIRECTORY;
    public static File DATAPACK_ORDERING_FILE;
    public static File RESOURCEPACK_ORDERING_FILE;
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static void init() {
    }
}
