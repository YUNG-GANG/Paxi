package com.yungnickyoung.minecraft.paxi;

import net.minecraft.resource.*;

import java.io.File;
import java.io.FileFilter;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Reimplementation of {@link FileResourcePackProvider} with Paxi pack source hardcoded in.
 * Also ensures that any resource packs created from this provider are always enabled.
 */
public class PaxiFileResourcePackProvider extends FileResourcePackProvider {
    private static final FileFilter POSSIBLE_PACK = (file) -> {
        boolean bl = file.isFile() && file.getName().endsWith(".zip");
        boolean bl2 = file.isDirectory() && (new File(file, "pack.mcmeta")).isFile();
        return bl || bl2;
    };

    public PaxiFileResourcePackProvider(File packsFolder) {
        super(packsFolder, PaxiResourcePackSource.PACK_SOURCE_PAXI);
    }

    @Override
    public void register(Consumer<ResourcePackProfile> profileAdder, ResourcePackProfile.Factory factory) {
        if (!this.packsFolder.isDirectory()) {
            this.packsFolder.mkdirs();
        }

        File[] packs = this.packsFolder.listFiles(POSSIBLE_PACK);
        if (packs != null) {
            for (File file : packs) {
                String packName = file.getName();
                ResourcePackProfile resourcePackProfile = ResourcePackProfile.of(
                    packName,
                    true,
                    this.createResourcePack(file),
                    factory,
                    ResourcePackProfile.InsertionPosition.TOP,
                    PaxiResourcePackSource.PACK_SOURCE_PAXI);

                if (resourcePackProfile != null) {
                    profileAdder.accept(resourcePackProfile);
                }
            }

        }
    }

    private Supplier<ResourcePack> createResourcePack(File file) {
        return file.isDirectory()
            ? () -> new DirectoryResourcePack(file)
            : () -> new ZipResourcePack(file);
    }
}
