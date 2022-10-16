package com.yungnickyoung.minecraft.paxi.mixin;

import com.yungnickyoung.minecraft.paxi.PaxiCommon;
import com.yungnickyoung.minecraft.paxi.PaxiRepositorySource;
import com.yungnickyoung.minecraft.paxi.util.IPaxiSourceProvider;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackCreator;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ModResourcePackCreator.class)
public class MixinModResourcePackCreator implements IPaxiSourceProvider {
    @Unique
    private PaxiRepositorySource paxiRepositorySource;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void paxi_addPaxiRepositorySource(PackType type, CallbackInfo callback) {
        if (type == PackType.SERVER_DATA) {
            this.paxiRepositorySource = new PaxiRepositorySource(PaxiCommon.DATA_PACK_DIRECTORY, PaxiCommon.DATAPACK_ORDERING_FILE);
        } else if (type == PackType.CLIENT_RESOURCES) {
            this.paxiRepositorySource = new PaxiRepositorySource(PaxiCommon.RESOURCE_PACK_DIRECTORY);
        }
    }

    @Inject(method = "loadPacks", at = @At("RETURN"))
    private void paxi_loadPaxiPacks(Consumer<Pack> consumer, Pack.PackConstructor factory, CallbackInfo callback) {
        if (this.paxiRepositorySource != null) {
            this.paxiRepositorySource.loadPacks(consumer, factory);
        }
    }

    @Override
    public PaxiRepositorySource getPaxiSource() {
        return this.paxiRepositorySource;
    }
}