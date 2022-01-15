package com.yungnickyoung.minecraft.paxi.client;

import com.yungnickyoung.minecraft.paxi.Paxi;
import com.yungnickyoung.minecraft.paxi.PaxiFileResourcePackProvider;
import com.yungnickyoung.minecraft.paxi.mixin.FolderRepositorySourceAccessor;
import com.yungnickyoung.minecraft.paxi.mixin.PackRepositoryAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;

import java.util.HashSet;

public class PaxiClient {
    public static void init() {
        addResourcePackProvider();
    }

    private static void addResourcePackProvider() {
        PackRepository resourcePackManager = Minecraft.getInstance().getResourcePackRepository();

        // New provider for the Paxi directory
        PaxiFileResourcePackProvider newProvider = new PaxiFileResourcePackProvider(Paxi.RESOURCE_PACK_DIRECTORY);

        // Set is immutable by default, so we recreate it here
        ((PackRepositoryAccessor) resourcePackManager).setSources(new HashSet<>(((PackRepositoryAccessor) resourcePackManager).getSources()));

        // Ensure provider isn't already added
        for (RepositorySource provider : ((PackRepositoryAccessor) resourcePackManager).getSources()) {
            if (
                provider instanceof FolderRepositorySource &&
                ((FolderRepositorySourceAccessor) provider).getFolder().getAbsolutePath().equals(Paxi.RESOURCE_PACK_DIRECTORY.getAbsolutePath())
            ) {
                Paxi.LOGGER.info("Paxi global resource pack provider already exists. Skipping...");
                return;
            }
        }

        // Add provider
        Paxi.LOGGER.info("Adding global resource pack provider...");
        ((PackRepositoryAccessor) resourcePackManager).getSources().add(newProvider);
    }
}
