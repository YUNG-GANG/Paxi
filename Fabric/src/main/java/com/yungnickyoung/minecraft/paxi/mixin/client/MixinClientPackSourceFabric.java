package com.yungnickyoung.minecraft.paxi.mixin.client;

import com.yungnickyoung.minecraft.paxi.PaxiCommon;
import com.yungnickyoung.minecraft.paxi.PaxiRepositorySource;
import com.yungnickyoung.minecraft.paxi.util.IPaxiSourceProvider;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ClientPackSource.class)
public class MixinClientPackSourceFabric implements IPaxiSourceProvider {
    @Unique
    private PaxiRepositorySource paxiRepositorySource;

    @Inject(method = "loadPacks", at = @At("RETURN"))
    private void paxi_loadPaxiPabcksClientFabric(Consumer<Pack> consumer, Pack.PackConstructor factory, CallbackInfo callback) {
        if (this.paxiRepositorySource == null) {
            this.paxiRepositorySource = new PaxiRepositorySource(PaxiCommon.RESOURCE_PACK_DIRECTORY, PaxiCommon.RESOURCEPACK_ORDERING_FILE);
        }
        this.paxiRepositorySource.loadPacks(consumer, factory);
    }

    @Override
    public PaxiRepositorySource getPaxiSource() {
        return this.paxiRepositorySource;
    }
}
