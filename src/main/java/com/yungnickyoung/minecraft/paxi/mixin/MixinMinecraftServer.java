package com.yungnickyoung.minecraft.paxi.mixin;

import com.yungnickyoung.minecraft.paxi.Paxi;
import com.yungnickyoung.minecraft.paxi.PaxiFileResourcePackProvider;
import com.yungnickyoung.minecraft.paxi.PaxiResourcePackSource;
import net.minecraft.resource.*;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
    @Inject(at=@At("HEAD"), method="loadDataPacks")
    private static void loadDataPacks(ResourcePackManager resourcePackManager, DataPackSettings dataPackSettings, boolean safeMode, CallbackInfoReturnable<DataPackSettings> info) {
        if (!safeMode) {
            // New provider for the Paxi directory
            PaxiFileResourcePackProvider newProvider = new PaxiFileResourcePackProvider(Paxi.DATA_PACK_DIRECTORY);

            // Set is immutable by default, so we recreate it here
            resourcePackManager.providers = new HashSet<>(resourcePackManager.providers);

            // Ensure provider isn't already added
            for (ResourcePackProvider provider : resourcePackManager.providers) {
                if (
                    provider instanceof FileResourcePackProvider &&
                    ((FileResourcePackProvider) provider).packsFolder.getAbsolutePath().equals(Paxi.DATA_PACK_DIRECTORY.getAbsolutePath())
                ) {
                    Paxi.LOGGER.info("Paxi global data pack provider already exists. Skipping...");
                    return; // Provider already exists
                }
            }

            // Add and enable provider
            Paxi.LOGGER.info("Adding global data pack provider...");
            resourcePackManager.providers.add(newProvider);
        }
    }
}
