package com.yungnickyoung.minecraft.paxi;

import com.google.common.collect.Lists;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.yungnickyoung.minecraft.paxi.mixin.accessor.FolderRepositorySourceAccessor;
import com.yungnickyoung.minecraft.yungsapi.io.JSON;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.FolderPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.Pack;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Reimplementation of {@link FolderRepositorySource} with Paxi pack source hardcoded in.
 * Ensures that any data/resource packs created from this provider are always enabled.
 * Supports an ordering JSON file for loading codependent data packs.
 */
public class PaxiRepositorySource extends FolderRepositorySource {
    private static final FileFilter PACK_FILTER = (file) -> {
        boolean isValidPackZip = file.isFile() && file.getName().endsWith(".zip");
        boolean isValidPackFolder = file.isDirectory() && (new File(file, "pack.mcmeta")).isFile();
        return isValidPackZip || isValidPackFolder;
    };

    private File ordering;
    public List<String> orderedPaxiPacks = new ArrayList<>();
    public List<String> unorderedPaxiPacks = new ArrayList<>();

    public PaxiRepositorySource(File packsFolder, File ordering) {
        super(packsFolder, PaxiPackSource.PACK_SOURCE_PAXI);
        this.ordering = ordering;
    }

    public PaxiRepositorySource(File packsFolder) {
        super(packsFolder, PaxiPackSource.PACK_SOURCE_PAXI);
    }

    @Override
    public void loadPacks(Consumer<Pack> profileAdder, Pack.PackConstructor packConstructor) {
        // Initialize directories
        File folder = ((FolderRepositorySourceAccessor) this).getFolder();
        if (folder == null) {
            return;
        }
        if (!folder.isDirectory()) {
            ((FolderRepositorySourceAccessor) this).getFolder().mkdirs();
        }

        // Initialize ordering file if it doesn't already exist
        if (ordering != null && !ordering.isFile()) {
            PackOrdering emptyPackOrdering = new PackOrdering(new String[]{});
            try {
                JSON.createJsonFileFromObject(ordering.toPath(), emptyPackOrdering);
            } catch (IOException e) {
                PaxiCommon.LOGGER.error("Unable to create default pack ordering file! This shouldn't happen.");
                PaxiCommon.LOGGER.error(e.toString());
            }
        }

        File[] packs = loadPacksFromFiles();

        if (packs != null) {
            for (File file : packs) {
                String packName = file.getName();
                Pack resourcePackProfile = Pack.create(
                    packName,
                    true,
                    this.createPackResourcesSupplier(file),
                    packConstructor,
                    Pack.Position.TOP,
                    PaxiPackSource.PACK_SOURCE_PAXI);

                if (resourcePackProfile != null) {
                    profileAdder.accept(resourcePackProfile);
                }
            }
        }
    }

    /**
     * Builds an array of Files corresponding to the valid packs in this object's packsFolder.
     *
     * If this pack provider has an ordering File defined, the returned array will contained the specified Files
     * in the proper order, with any unspecified Files appended to the end of the List.
     *
     * If this pack provider does not have an ordering File defined, the returned array's items have no guaranteed order.
     */
    private File[] loadPacksFromFiles() {
        if (this.ordering != null) {
            // If ordering file exists, load any specified files in the specific order
            PackOrdering packOrdering = null;
            try {
                packOrdering = JSON.loadObjectFromJsonFile(ordering.toPath(), PackOrdering.class);
            } catch (IOException | JsonIOException | JsonSyntaxException e) {
                PaxiCommon.LOGGER.error("Error loading Paxi ordering JSON file {}: {}", this.ordering.getName(), e.toString());
            }

            // Check that we loaded ordering properly
            if (packOrdering == null) {
                // If loading the ordering failed, we default to random ordering
                PaxiCommon.LOGGER.error("Unable to load ordering JSON file {}! Is it proper JSON formatting? Ignoring load order...", this.ordering.getName());
                return ((FolderRepositorySourceAccessor) this).getFolder().listFiles(PACK_FILTER);
            } else if (packOrdering.getOrderedPackNames() == null) {
                // User probably mistyped the "loadOrder" key - Let them know and default to random order
                PaxiCommon.LOGGER.error("Unable to find entry with name 'loadOrder' in load ordering JSON file {}! Ignoring load order...", this.ordering.getName());
                return ((FolderRepositorySourceAccessor) this).getFolder().listFiles(PACK_FILTER);
            } else {
                // If loading ordering succeeded, we first load the ordered packs
                List<File> orderedPacks = filesFromNames(packOrdering.getOrderedPackNames(), PACK_FILTER);

                orderedPacks.forEach(file -> this.orderedPaxiPacks.add(file.getName()));

                // Next we prepend any leftover packs with unspecified order
                File[] allPacks = ((FolderRepositorySourceAccessor) this).getFolder().listFiles(PACK_FILTER);
                List<File> unorderedPacks = allPacks == null
                        ? Lists.newArrayList()
                        : Arrays.stream(allPacks).filter(file -> !orderedPacks.contains(file)).collect(Collectors.toList());

                orderedPacks.forEach(file -> this.orderedPaxiPacks.add(file.getName()));
                unorderedPacks.forEach(file -> this.unorderedPaxiPacks.add(file.getName()));

                return Stream.of(unorderedPacks, orderedPacks).flatMap(Collection::stream).toArray(File[]::new);
            }
        } else {
            // If ordering file doesn't exist, load files in any order
            return ((FolderRepositorySourceAccessor) this).getFolder().listFiles(PACK_FILTER);
        }
    }

    /**
     * Creates a List of File objects created from the provided file names.
     * Each File must pass the provided filter to be added to the List.
     */
    private List<File> filesFromNames(String[] packFileNames, FileFilter filter) {
        ArrayList<File> packFiles = new ArrayList<>();

        for (String fileName : packFileNames) {
            File packFile = new File(((FolderRepositorySourceAccessor) this).getFolder(), fileName);

            if (!packFile.exists()) {
                PaxiCommon.LOGGER.error("Unable to find pack with name {} specified in load ordering JSON file {}! Skipping...", fileName, this.ordering.getName());
            } else if ((filter == null) || filter.accept(packFile)) {
                packFiles.add(packFile);
            }
        }
        return packFiles;
    }

    /**
     * Creates the proper ResourcePack supplier for the given file.
     * Assumes that the provided file has already been validated as a properly formatted pack.
     */
    private Supplier<PackResources> createPackResourcesSupplier(File file) {
        return file.isDirectory()
            ? () -> new FolderPackResources(file)
            : () -> new FilePackResources(file);
    }

    public boolean hasPacks() {
        return this.unorderedPaxiPacks.size() > 0 || this.orderedPaxiPacks.size() > 0;
    }

    /**
     * Record for JSON load order serialization.
     */
    private static class PackOrdering {
        @SerializedName("loadOrder")
        private String[] orderedPackNames;

        public PackOrdering(String[] orderedPackNames) {
            this.orderedPackNames = orderedPackNames;
        }

        public String[] getOrderedPackNames() {
            return orderedPackNames;
        }
    }
}
