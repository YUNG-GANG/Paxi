package com.yungnickyoung.minecraft.paxi.services;

import com.yungnickyoung.minecraft.paxi.client.PaxiClientCommon;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class ForgeModulesLoader implements IModulesLoader {
    @Override
    public void loadModules() {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> PaxiClientCommon::init);
    }
}
