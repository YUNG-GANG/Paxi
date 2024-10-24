package com.yungnickyoung.minecraft.paxi.mixin.client;

import com.yungnickyoung.minecraft.paxi.PaxiCommon;
import com.yungnickyoung.minecraft.paxi.PaxiRepositorySource;
import com.yungnickyoung.minecraft.paxi.util.IPaxiSourceProvider;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

/**
 * Attaches a PaxiRepositorySource to the ClientPackSource.
 */
@Mixin(BuiltInPackSource.class)
public class MixinClientPackSourceFabric implements IPaxiSourceProvider {
    @Unique
    private PaxiRepositorySource paxiRepositorySource;

    @Inject(method = "loadPacks", at = @At("RETURN"))
    private void paxi_loadPaxiPacksClientFabric(Consumer<Pack> consumer, CallbackInfo callback) {
        if (isClientPackSource(this)) {
            if (this.paxiRepositorySource == null) {
                this.paxiRepositorySource = new PaxiRepositorySource(PaxiCommon.RESOURCE_PACK_DIRECTORY, PackType.CLIENT_RESOURCES, PaxiCommon.RESOURCEPACK_ORDERING_FILE);
            }
            this.paxiRepositorySource.loadPacks(consumer);
        }
    }

    @Override
    public PaxiRepositorySource getPaxiSource() {
        return this.paxiRepositorySource;
    }

    @Unique
    private boolean isClientPackSource(Object obj) {
        return obj instanceof ClientPackSource;
    }
}
