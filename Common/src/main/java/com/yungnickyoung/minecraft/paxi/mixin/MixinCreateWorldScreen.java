package com.yungnickyoung.minecraft.paxi.mixin;

import com.yungnickyoung.minecraft.paxi.PaxiCommon;
import com.yungnickyoung.minecraft.paxi.PaxiFileResourcePackProvider;
import com.yungnickyoung.minecraft.paxi.mixin.accessor.FolderRepositorySourceAccessor;
import com.yungnickyoung.minecraft.paxi.mixin.accessor.PackRepositoryAccessor;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

/**
 * Manually adds the Paxi provider to the ResourcePackManager when the data pack selection screen is opened from the Create World Screen.
 * This ensures that Paxi packs appear in the data pack selection screen and are added to the world in the correct order.
 * */
@Mixin(CreateWorldScreen.class)
public abstract class MixinCreateWorldScreen {

    @Shadow
    @Nullable
    private PackRepository tempDataPackRepository;

    // We inject after the pack selection settings have been loaded so that Paxi packs appear in the data pack selection screen
    @Inject(method = "openDataPackSelectionScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/worldselection/CreateWorldScreen;getDataPackSelectionSettings()Lcom/mojang/datafixers/util/Pair;", shift = At.Shift.AFTER))
    private void onOpenDatapackSelectionScreen(CallbackInfo ci) {
        // This should be set by the call to getDataPackSelectionSettings
        PackRepository packRepository = this.tempDataPackRepository;

        if (packRepository != null) {
            for (RepositorySource provider : ((PackRepositoryAccessor) packRepository).getSources()) {
                if (provider instanceof FolderRepositorySource && ((FolderRepositorySourceAccessor) provider).getFolder().getAbsolutePath().equals(PaxiCommon.DATA_PACK_DIRECTORY.getAbsolutePath())) {
                    PaxiCommon.LOGGER.info("Paxi global data pack provider already exists. Skipping...");
                    return;
                }
            }

            PaxiFileResourcePackProvider newProvider = new PaxiFileResourcePackProvider(PaxiCommon.DATA_PACK_DIRECTORY, PaxiCommon.DATAPACK_ORDERING_FILE);
            PaxiCommon.LOGGER.info("Adding global data pack provider...");
            ((PackRepositoryAccessor) packRepository).getSources().add(newProvider);
            // We reload here to ensure that Paxi packs are enabled and in the correct order.
            packRepository.reload();
        }
    }
}