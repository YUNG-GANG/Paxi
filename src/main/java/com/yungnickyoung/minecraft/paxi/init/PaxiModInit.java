package com.yungnickyoung.minecraft.paxi.init;

import com.yungnickyoung.minecraft.paxi.Paxi;
import com.yungnickyoung.minecraft.paxi.PaxiFileResourcePackProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.FolderPackFinder;
import net.minecraft.resources.IPackFinder;
import net.minecraft.resources.ResourcePackList;

import java.util.HashSet;

public class PaxiModInit {
    public static void init() {
        addResourcePackProvider();
    }

    private static void addResourcePackProvider() {
        ResourcePackList resourcePackManager = Minecraft.getInstance().getResourcePackList();

        // New provider for the Paxi directory
        PaxiFileResourcePackProvider newProvider = new PaxiFileResourcePackProvider(Paxi.RESOURCE_PACK_DIRECTORY);

        // Set is immutable by default, so we recreate it here
        resourcePackManager.packFinders = new HashSet<>(resourcePackManager.packFinders);

        // Ensure provider isn't already added
        for (IPackFinder provider : resourcePackManager.packFinders) {
            if (
                provider instanceof FolderPackFinder &&
                    ((FolderPackFinder) provider).folder.getAbsolutePath().equals(Paxi.RESOURCE_PACK_DIRECTORY.getAbsolutePath())
            ) {
                Paxi.LOGGER.info("Paxi global resource pack provider already exists. Skipping...");
                return;
            }
        }

        // Add provider
        Paxi.LOGGER.info("Adding global resource pack provider...");
        resourcePackManager.packFinders.add(newProvider);
    }
}
