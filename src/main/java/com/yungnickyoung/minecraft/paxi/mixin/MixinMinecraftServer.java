package com.yungnickyoung.minecraft.paxi.mixin;

import com.yungnickyoung.minecraft.paxi.Paxi;
import com.yungnickyoung.minecraft.paxi.PaxiFileResourcePackProvider;
import net.minecraft.resources.FolderPackFinder;
import net.minecraft.resources.IPackFinder;
import net.minecraft.resources.ResourcePackList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.codec.DatapackCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;

/**
 * Manually adds the Paxi provider to the ResourcePackManager when loading a server's data packs.
 * This occurs right before a call to {@link ResourcePackList#reloadPacksFromFinders()}, which calls the provider's
 * register method and then adds all provided Paxi pack profiles to the pack manager.
 */
@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
    @Inject(at=@At("HEAD"), method="func_240772_a_")
    private static void loadDataPacks(ResourcePackList resourcePackManager, DatapackCodec dataPackSettings, boolean safeMode, CallbackInfoReturnable<DatapackCodec> info) {
        if (!safeMode) {
            // New provider for the Paxi directory
            PaxiFileResourcePackProvider newProvider = new PaxiFileResourcePackProvider(Paxi.DATA_PACK_DIRECTORY, Paxi.DATAPACK_ORDERING_FILE);

            // Recreate set in case it is immutable
            resourcePackManager.packFinders = new HashSet<>(resourcePackManager.packFinders);

            // Ensure provider isn't already added
            for (IPackFinder provider : resourcePackManager.packFinders) {
                if (
                    provider instanceof FolderPackFinder &&
                    ((FolderPackFinder) provider).folder.getAbsolutePath().equals(Paxi.DATA_PACK_DIRECTORY.getAbsolutePath())
                ) {
                    Paxi.LOGGER.info("Paxi global data pack provider already exists. Skipping...");
                    return;
                }
            }

            // Add and enable provider
            Paxi.LOGGER.info("Adding global data pack provider...");
            resourcePackManager.packFinders.add(newProvider);
        }
    }
}
