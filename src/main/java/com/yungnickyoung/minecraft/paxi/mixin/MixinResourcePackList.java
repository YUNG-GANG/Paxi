package com.yungnickyoung.minecraft.paxi.mixin;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.yungnickyoung.minecraft.paxi.PaxiFileResourcePackProvider;
import net.minecraft.resources.IPackFinder;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.resources.ResourcePackList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Overwrites the vanilla method for building a list of enabled resource packs.
 * Should completely preserve vanilla behavior while adding Paxi packs separately in a specific order
 * as determined by the user's datapack_load_order.json.
 */
@Mixin(ResourcePackList.class)
public abstract class MixinResourcePackList {
    @Shadow
    private Map<String, ResourcePackInfo> packNameToInfo;

    @Shadow
    public Set<IPackFinder> packFinders;

    @Shadow
    private Stream<ResourcePackInfo> func_232620_c_(Collection<String> names) {
        throw new AssertionError();
    }

    @Inject(at=@At("HEAD"), method="func_232618_b_", cancellable = true)
    private void buildEnabledProfiles(Collection<String> enabledNames, CallbackInfoReturnable<List<ResourcePackInfo>> cir) {
        // Fetch Paxi pack provider
        Optional<IPackFinder> paxiProvider = this.packFinders.stream().filter(provider -> provider instanceof PaxiFileResourcePackProvider).findFirst();

        // List of all packs to be marked as enabled
        List<ResourcePackInfo> allEnabledPacks = this.func_232620_c_(enabledNames).collect(Collectors.toList());

        // List of all packs loaded by Paxi
        List<ResourcePackInfo> paxiPacks = new ArrayList<>();

        // Grab a list of all Paxi packs from the Paxi provider, if it exists.
        // We must gather Paxi packs separately because vanilla uses a TreeMap to store all packs, so they are
        // stored lexicographically, but for Paxi we need them to be enabled in a specific order
        // (determined by the user's datapack_load_order.json)
        if (paxiProvider.isPresent() && ((PaxiFileResourcePackProvider)paxiProvider.get()).orderedPaxiPacks.size() > 0) {
            paxiPacks = this.func_232620_c_(((PaxiFileResourcePackProvider)paxiProvider.get()).orderedPaxiPacks).collect(Collectors.toList());
            allEnabledPacks.removeAll(paxiPacks);
        }

        // Register all Paxi packs
        for (ResourcePackInfo resourcePackProfile : paxiPacks) {
            if (resourcePackProfile.isAlwaysEnabled() && !allEnabledPacks.contains(resourcePackProfile)) {
                resourcePackProfile.getPriority().insert(allEnabledPacks, resourcePackProfile, Functions.identity(), false);
            }
        }

        // Register all other packs (lexicographical order)
        for (ResourcePackInfo resourcePackProfile : this.packNameToInfo.values()) {
            if (resourcePackProfile.isAlwaysEnabled() && !allEnabledPacks.contains(resourcePackProfile)) {
                resourcePackProfile.getPriority().insert(allEnabledPacks, resourcePackProfile, Functions.identity(), false);
            }
        }

        cir.setReturnValue(ImmutableList.copyOf(allEnabledPacks));
    }
}
