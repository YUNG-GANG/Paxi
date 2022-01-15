package com.yungnickyoung.minecraft.paxi.mixin;

import com.yungnickyoung.minecraft.paxi.Paxi;
import com.yungnickyoung.minecraft.paxi.PaxiFileResourcePackProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.world.level.DataPackConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;

/**
 * Manually adds the Paxi provider to the ResourcePackManager when loading a server's data packs.
 * This occurs right before a call to {@link PackRepository#reload()}, which calls the provider's
 * register method and then adds all provided Paxi pack profiles to the pack manager.
 */
@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
    @Inject(at=@At("HEAD"), method="configurePackRepository")
    private static void loadDataPacks(PackRepository resourcePackManager, DataPackConfig dataPackSettings, boolean safeMode, CallbackInfoReturnable<DataPackConfig> info) {
        if (!safeMode) {
            // New provider for the Paxi directory
            PaxiFileResourcePackProvider newProvider = new PaxiFileResourcePackProvider(Paxi.DATA_PACK_DIRECTORY, Paxi.DATAPACK_ORDERING_FILE);

            // Set is immutable by default, so we recreate it here
            ((PackRepositoryAccessor) resourcePackManager).setSources(new HashSet<>(((PackRepositoryAccessor) resourcePackManager).getSources()));

            // Ensure provider isn't already added
            for (RepositorySource provider : ((PackRepositoryAccessor) resourcePackManager).getSources()) {
                if (
                    provider instanceof FolderRepositorySource &&
                            ((FolderRepositorySourceAccessor) provider).getFolder().getAbsolutePath().equals(Paxi.DATA_PACK_DIRECTORY.getAbsolutePath())
                ) {
                    Paxi.LOGGER.info("Paxi global data pack provider already exists. Skipping...");
                    return;
                }
            }

            // Add and enable provider
            Paxi.LOGGER.info("Adding global data pack provider...");
            ((PackRepositoryAccessor) resourcePackManager).getSources().add(newProvider);
        }
    }
}
