package com.yungnickyoung.minecraft.paxi.mixin;

import com.google.common.collect.ImmutableList;
import com.yungnickyoung.minecraft.paxi.PaxiRepositorySource;
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Overwrites the vanilla method for building a list of enabled packs.
 * Should completely preserve vanilla behavior while adding Paxi packs separately in a specific order
 * as determined by the user's load order json.
 */
@Mixin(PackRepository.class)
public abstract class MixinPackRepositoryForge {
    @Shadow
    private Map<String, Pack> available;

    @Final
    @Shadow
    private Set<RepositorySource> sources;

    @Shadow
    private Stream<Pack> getAvailablePacks(Collection<String> names) {
        throw new AssertionError();
    }

    @Inject(at=@At("HEAD"), method="rebuildSelected", cancellable = true)
    private void paxi_buildEnabledProfilesForge(Collection<String> enabledNames, CallbackInfoReturnable<List<Pack>> cir) {
        // Fetch Paxi pack repository source
        Optional<RepositorySource> paxiRepositorySource = this.sources.stream()
                .filter(provider -> provider instanceof PaxiRepositorySource)
                .findFirst();

        // List of all packs to be marked as enabled
        List<Pack> allEnabledPacks = this.getAvailablePacks(enabledNames).collect(Collectors.toList());

        // List of all packs loaded by Paxi
        List<Pack> paxiPacks = new ArrayList<>();

        // Grab a list of all Paxi packs from the Paxi repo source, if it exists.
        // We must gather Paxi packs separately because vanilla uses a TreeMap to store all packs, so they are
        // stored lexicographically, but for Paxi we need them to be enabled in a specific order
        // (determined by the user's datapack_load_order.json)
        if (paxiRepositorySource.isPresent() && !((PaxiRepositorySource) paxiRepositorySource.get()).orderedPaxiPacks.isEmpty()) {
            paxiPacks = this.getAvailablePacks(((PaxiRepositorySource)paxiRepositorySource.get()).orderedPaxiPacks).toList();
            allEnabledPacks.removeAll(paxiPacks);
        }

        // Register all Paxi packs
        for (Pack pack : paxiPacks) {
            if (pack.isRequired() && !allEnabledPacks.contains(pack)) {
                pack.getDefaultPosition().insert(allEnabledPacks, pack, Pack::selectionConfig, false);
            }
        }

        // Register all other packs (lexicographical order)
        for (Pack pack : this.available.values()) {
            if (pack.isRequired() && !allEnabledPacks.contains(pack)) {
                pack.getDefaultPosition().insert(allEnabledPacks, pack, Pack::selectionConfig, false);
            }
        }

        cir.setReturnValue(ImmutableList.copyOf(allEnabledPacks));
    }
}
