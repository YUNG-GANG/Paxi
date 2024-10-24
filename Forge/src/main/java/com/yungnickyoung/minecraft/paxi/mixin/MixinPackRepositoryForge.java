package com.yungnickyoung.minecraft.paxi.mixin;

import com.google.common.collect.ImmutableList;
import com.yungnickyoung.minecraft.paxi.PaxiRepositorySource;
import net.minecraft.Util;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Overwrites the vanilla method for building a list of enabled packs.
 * Should completely preserve vanilla behavior while adding Paxi packs separately in a specific order
 * as determined by the user's load order json.
 * Priority is set to 2000 to ensure that this mixin runs after all other mixins.
 */
@Mixin(value = PackRepository.class, priority = 2000)
public abstract class MixinPackRepositoryForge {
    @Final
    @Shadow
    private Set<RepositorySource> sources;

    @Shadow
    private Stream<Pack> getAvailablePacks(Collection<String> names) {
        throw new AssertionError();
    }

    @Inject(at = @At("RETURN"), method = "rebuildSelected", cancellable = true)
    private void paxi_buildEnabledProfilesForge(Collection<String> enabledNames, CallbackInfoReturnable<List<Pack>> cir) {
        List<Pack> sortedEnabledPacks = cir.getReturnValue().stream().collect(Util.toMutableList());

        // Fetch Paxi pack repository source
        Optional<RepositorySource> paxiRepositorySource = this.sources.stream()
                .filter(provider -> provider instanceof PaxiRepositorySource)
                .findFirst();

        // Grab a list of all ordered Paxi packs from the Paxi repo source, if it exists.
        // We must gather Paxi packs separately because vanilla uses a TreeMap to store all packs, so they are
        // stored lexicographically, but for Paxi we need them to be enabled in a specific order
        // (determined by the user's load order JSON)
        List<Pack> orderedPaxiPacks = new ArrayList<>();
        if (paxiRepositorySource.isPresent() && !((PaxiRepositorySource) paxiRepositorySource.get()).orderedPaxiPacks.isEmpty()) {
            orderedPaxiPacks = this.getAvailablePacks(((PaxiRepositorySource) paxiRepositorySource.get()).orderedPaxiPacks).toList();
            sortedEnabledPacks.removeAll(orderedPaxiPacks); // Ordered packs should always load after all other packs, so remove them for now
        }

        // Add all ordered Paxi packs
        for (Pack pack : orderedPaxiPacks) {
            if (pack.isRequired() && !sortedEnabledPacks.contains(pack)) {
                pack.getDefaultPosition().insert(sortedEnabledPacks, pack, Pack::selectionConfig, false);
            }
        }

        cir.setReturnValue(ImmutableList.copyOf(sortedEnabledPacks));
    }
}
