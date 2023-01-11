package com.yungnickyoung.minecraft.paxi.client;

import com.yungnickyoung.minecraft.paxi.util.IPaxiSourceProvider;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.server.packs.repository.RepositorySource;

import java.util.Optional;
import java.util.Set;

public class ClientMixinUtil {
    public static Optional<RepositorySource> getClientRepositorySource(Set<RepositorySource> sources) {
        Optional<ClientPackSource> clientPackSource = sources.stream()
                .filter(provider -> provider instanceof ClientPackSource)
                .findFirst()
                .map(repositorySource -> (ClientPackSource) repositorySource);
        return clientPackSource.map(packSource -> ((IPaxiSourceProvider) packSource).getPaxiSource());
    }
}
