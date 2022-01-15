package com.yungnickyoung.minecraft.paxi.mixin;

import net.minecraft.server.packs.repository.FolderRepositorySource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.io.File;

@Mixin(FolderRepositorySource.class)
public interface FolderRepositorySourceAccessor {
    @Accessor
    File getFolder();
}
