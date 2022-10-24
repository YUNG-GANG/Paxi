package com.yungnickyoung.minecraft.paxi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class PaxiCommon {
    public static final String MOD_ID = "paxi";
    public static final String MOD_NAME = "Paxi";
    public static File BASE_PACK_DIRECTORY;
    public static File DATA_PACK_DIRECTORY;
    public static File RESOURCE_PACK_DIRECTORY;
    public static File DATAPACK_ORDERING_FILE;
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static void init() {
    }
}
