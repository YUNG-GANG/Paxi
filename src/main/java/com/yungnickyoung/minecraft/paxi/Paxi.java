package com.yungnickyoung.minecraft.paxi;

import com.yungnickyoung.minecraft.paxi.init.PaxiModInit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(Paxi.MOD_ID)
public class Paxi {
    public static final String MOD_ID = "paxi";
    public static final File BASE_PACK_DIRECTORY = new File(FMLPaths.CONFIGDIR.get().toString(), "paxi");
    public static final File DATA_PACK_DIRECTORY = new File(BASE_PACK_DIRECTORY, "datapacks");
    public static final File RESOURCE_PACK_DIRECTORY = new File(BASE_PACK_DIRECTORY, "resourcepacks");
    public static final File DATAPACK_ORDERING_FILE = new File(BASE_PACK_DIRECTORY, "datapack_load_order.json");
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public Paxi() {
        init();
    }

    private void init() {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> PaxiModInit::init);
    }
}
