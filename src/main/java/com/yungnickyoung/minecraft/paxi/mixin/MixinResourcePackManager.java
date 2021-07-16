package com.yungnickyoung.minecraft.paxi.mixin;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.yungnickyoung.minecraft.paxi.PaxiFileResourcePackProvider;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Overwrites the vanilla method for building a list of enabled resource packs.
 * Should completely preserve vanilla behavior while adding Paxi packs separately in a specific order
 * as determined by the user's datapack_load_order.json.
 */
@Mixin(ResourcePackManager.class)
public abstract class MixinResourcePackManager {
    @Shadow
    private Map<String, ResourcePackProfile> profiles;

    @Shadow
    public Set<ResourcePackProvider> providers;

    @Shadow
    private Stream<ResourcePackProfile> streamProfilesByName(Collection<String> names) {
        throw new AssertionError();
    }

    private List<ResourcePackProfile> buildEnabledProfiles(Collection<String> enabledNames) {
        // Fetch Paxi pack provider
        Optional<ResourcePackProvider> paxiProvider = this.providers.stream().filter(provider -> provider instanceof PaxiFileResourcePackProvider).findFirst();

        // List of all packs to be marked as enabled
        List<ResourcePackProfile> allEnabledPacks = this.streamProfilesByName(enabledNames).collect(Collectors.toList());

        // List of all packs loaded by Paxi
        List<ResourcePackProfile> paxiPacks = new ArrayList<>();

        // Grab a list of all Paxi packs from the Paxi provider, if it exists.
        // We must gather Paxi packs separately because vanilla uses a TreeMap to store all packs, so they are
        // stored lexicographically, but for Paxi we need them to be enabled in a specific order
        // (determined by the user's datapack_load_order.json)
        if (paxiProvider.isPresent() && ((PaxiFileResourcePackProvider)paxiProvider.get()).orderedPaxiPacks.size() > 0) {
            paxiPacks = this.streamProfilesByName(((PaxiFileResourcePackProvider)paxiProvider.get()).orderedPaxiPacks).collect(Collectors.toList());
            allEnabledPacks.removeAll(paxiPacks);
        }

        // Register all Paxi packs
        for (ResourcePackProfile resourcePackProfile : paxiPacks) {
            if (resourcePackProfile.isAlwaysEnabled() && !allEnabledPacks.contains(resourcePackProfile)) {
                resourcePackProfile.getInitialPosition().insert(allEnabledPacks, resourcePackProfile, Functions.identity(), false);
            }
        }

        // Register all other packs (lexicographical order)
        for (ResourcePackProfile resourcePackProfile : this.profiles.values()) {
            if (resourcePackProfile.isAlwaysEnabled() && !allEnabledPacks.contains(resourcePackProfile)) {
                resourcePackProfile.getInitialPosition().insert(allEnabledPacks, resourcePackProfile, Functions.identity(), false);
            }
        }

        return ImmutableList.copyOf(allEnabledPacks);
    }
}
