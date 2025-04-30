package com.yungnickyoung.minecraft.paxi.mixin;

import com.google.common.collect.ImmutableList;
import com.yungnickyoung.minecraft.paxi.PaxiCommon;
import com.yungnickyoung.minecraft.paxi.PaxiRepositorySource;
import com.yungnickyoung.minecraft.paxi.client.ClientMixinUtil;
import com.yungnickyoung.minecraft.paxi.util.IPaxiSourceProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackCreator;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Overwrites the vanilla method for building a list of enabled packs.
 * Should completely preserve vanilla + Fabric behavior while adding Paxi packs separately in a specific order
 * as determined by the user's load order json.
 * Priority is set to 2000 to ensure that this mixin runs after all other Fabric mixins that modify the same method.
 */
@Mixin(value = PackRepository.class, priority = 2000)
public abstract class MixinPackRepositoryFabric {
    @Final
    @Shadow
    private Set<RepositorySource> sources;

    @Shadow
    private Stream<Pack> getAvailablePacks(Collection<String> names) {
        throw new AssertionError();
    }

    @Inject(at = @At("RETURN"), method = "discoverAvailable", cancellable = true)
    private void paxi_removeDuplicatesInAvailableList(CallbackInfoReturnable<Map<String, Pack>> cir) {
        // Paxi repo source. Will be fetched differently depending if we're loading data or resource packs.
        Optional<RepositorySource> repositorySource = getPaxiRepositorySource();
        if (repositorySource.isEmpty()) {
            PaxiCommon.LOGGER.error("Unable to find Paxi repository source when removing duplicates from available packs. You may see duplicate pack entries in your list of available packs on the Resource Packs screen.");
            return;
        }

        // Get a list of all ordered Paxi packs
        PaxiRepositorySource paxiRepositorySource = (PaxiRepositorySource) repositorySource.get();
        List<String> orderedPaxiPacks = paxiRepositorySource.orderedPaxiPacks;

        // Remove duplicates from the available packs list and return the new list
        Map<String, Pack> availablePacks = new TreeMap<>(cir.getReturnValue());
        Set<String> keysToRemove = new HashSet<>();
        for (String vanillaPackId : availablePacks.keySet()) {
            // Vanilla pack IDs are stored as "file/" + fileName,
            // but Paxi packs are stored as just the fileName.
            for (String paxiPackId : orderedPaxiPacks) {
                if (vanillaPackId.equals("file/" + paxiPackId)) {
                    keysToRemove.add(vanillaPackId);
                    break;
                }
            }
        }
        keysToRemove.forEach(availablePacks::remove);
        cir.setReturnValue(availablePacks);
    }

    @Inject(at = @At("RETURN"), method = "rebuildSelected", cancellable = true)
    private void paxi_buildEnabledProfilesFabric(Collection<String> enabledNames, CallbackInfoReturnable<List<Pack>> cir) {
        List<Pack> sortedEnabledPacks = cir.getReturnValue().stream().collect(Util.toMutableList());

        // Paxi repo source. Will be fetched differently depending if we're loading data or resource packs.
        Optional<RepositorySource> paxiRepositorySource = getPaxiRepositorySource();

        // List of all packs loaded by Paxi
        List<Pack> unorderedPaxiPacks = new ArrayList<>();
        List<Pack> orderedPaxiPacks = new ArrayList<>();

        // Grab a list of all Paxi packs from the Paxi repo source, if it exists.
        // We must gather Paxi packs separately because vanilla uses a TreeMap to store all packs, so they are
        // stored lexicographically, but for Paxi we need them to be enabled in a specific order
        // (determined by the user's load order json)
        if (paxiRepositorySource.isPresent() && ((PaxiRepositorySource) paxiRepositorySource.get()).hasPacks()) {
            unorderedPaxiPacks.addAll(this.getAvailablePacks(((PaxiRepositorySource) paxiRepositorySource.get()).unorderedPaxiPacks).toList());
            orderedPaxiPacks.addAll(this.getAvailablePacks(((PaxiRepositorySource) paxiRepositorySource.get()).orderedPaxiPacks).toList());
            sortedEnabledPacks.removeAll(orderedPaxiPacks); // Ordered packs should always load after all other packs, so remove them for now
        }

        // Add all Paxi packs
        Stream.concat(unorderedPaxiPacks.stream(), orderedPaxiPacks.stream()).forEach(pack -> {
            if (pack.isRequired() && !sortedEnabledPacks.contains(pack)) {
                pack.getDefaultPosition().insert(sortedEnabledPacks, pack, Pack::selectionConfig, false);
            }
        });

        cir.setReturnValue(ImmutableList.copyOf(sortedEnabledPacks));
    }

    @Unique
    private Optional<RepositorySource> getPaxiRepositorySource() {
        boolean isClient = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
        Optional<RepositorySource> paxiRepositorySource = Optional.empty();

        // Data-pack only
        Optional<ModResourcePackCreator> moddedPackRepositorySource = this.sources.stream()
                .filter(provider -> provider instanceof ModResourcePackCreator)
                .findFirst()
                .map(repositorySource -> (ModResourcePackCreator) repositorySource);
        if (moddedPackRepositorySource.isPresent()) {
            paxiRepositorySource = Optional.of(((IPaxiSourceProvider) moddedPackRepositorySource.get()).getPaxiSource());
        }

        // Resource-pack only.
        // Uses separate util method to avoid classloading client-only
        // classes when using Paxi on a dedicated server.
        if (paxiRepositorySource.isEmpty() && isClient) {
            paxiRepositorySource = ClientMixinUtil.getClientRepositorySource(this.sources);
        }

        return paxiRepositorySource;
    }
}
