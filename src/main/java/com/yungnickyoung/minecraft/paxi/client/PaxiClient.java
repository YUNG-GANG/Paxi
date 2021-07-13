package com.yungnickyoung.minecraft.paxi.client;

import com.yungnickyoung.minecraft.paxi.Paxi;
import com.yungnickyoung.minecraft.paxi.PaxiFileResourcePackProvider;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.FileResourcePackProvider;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProvider;

import java.util.HashSet;

public class PaxiClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        addResourcePackProvider();
    }

    private void addResourcePackProvider() {
        ResourcePackManager resourcePackManager = MinecraftClient.getInstance().getResourcePackManager();

        // New provider for the Paxi directory
        PaxiFileResourcePackProvider newProvider = new PaxiFileResourcePackProvider(Paxi.RESOURCE_PACK_DIRECTORY);

        // Set is immutable by default, so we recreate it here
        resourcePackManager.providers = new HashSet<>(resourcePackManager.providers);

        // Ensure provider isn't already added
        for (ResourcePackProvider provider : resourcePackManager.providers) {
            if (
                provider instanceof FileResourcePackProvider &&
                ((FileResourcePackProvider) provider).packsFolder.getAbsolutePath().equals(Paxi.RESOURCE_PACK_DIRECTORY.getAbsolutePath())
            ) {
                Paxi.LOGGER.info("Paxi global resource pack provider already exists. Skipping...");
                return;
            }
        }

        // Add provider
        Paxi.LOGGER.info("Adding global resource pack provider...");
        resourcePackManager.providers.add(newProvider);
    }
}
