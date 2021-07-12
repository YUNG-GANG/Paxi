package com.yungnickyoung.minecraft.paxi.mixin;

import com.yungnickyoung.minecraft.paxi.Paxi;
import net.minecraft.resource.*;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
	@Inject(at=@At("INVOKE"), method="Lnet/minecraft/server/MinecraftServer;loadDataPacks(Lnet/minecraft/resource/ResourcePackManager;Lnet/minecraft/resource/DataPackSettings;Z)Lnet/minecraft/resource/DataPackSettings;")
	private static void loadDataPacks(ResourcePackManager resourcePackManager, DataPackSettings dataPackSettings, boolean safeMode, CallbackInfoReturnable<DataPackSettings> info) {
		FileResourcePackProvider newProvider = new FileResourcePackProvider(Paxi.PACK_DIRECTORY, ResourcePackSource.PACK_SOURCE_NONE);
		resourcePackManager.providers =  new HashSet<>(resourcePackManager.providers);
		for (ResourcePackProvider provider : resourcePackManager.providers) {
			if (
					provider instanceof FileResourcePackProvider &&
							((FileResourcePackProvider)provider).packsFolder.getAbsolutePath().equals(Paxi.PACK_DIRECTORY.getAbsolutePath())
			) {
				return; // provider already exists
			}
		}
		resourcePackManager.providers.add(newProvider);
	}
}
