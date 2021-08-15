package com.yungnickyoung.minecraft.paxi;

import com.google.common.collect.Lists;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.yungnickyoung.minecraft.yungsapi.io.JSON;
import net.minecraft.resources.*;

import javax.annotation.ParametersAreNonnullByDefault;
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
 * Reimplementation of {@link FolderPackFinder} with Paxi pack source hardcoded in.
 * Ensures that any data/resource packs created from this provider are always enabled.
 * Supports an ordering JSON file for loading codependent data packs.
 */
public class PaxiFileResourcePackProvider extends FolderPackFinder {
    private static final FileFilter POSSIBLE_PACK = (file) -> {
        boolean isValidPackZip = file.isFile() && file.getName().endsWith(".zip");
        boolean isValidPackFolder = file.isDirectory() && (new File(file, "pack.mcmeta")).isFile();
        return isValidPackZip || isValidPackFolder;
    };

    private File ordering;
    public List<String> orderedPaxiPacks = new ArrayList<>();

    public PaxiFileResourcePackProvider(File packsFolder, File ordering) {
        super(packsFolder, PaxiResourcePackSource.PACK_SOURCE_PAXI);
        this.ordering = ordering;
    }

    public PaxiFileResourcePackProvider(File packsFolder) {
        super(packsFolder, PaxiResourcePackSource.PACK_SOURCE_PAXI);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void findPacks(Consumer<ResourcePackInfo> profileAdder, ResourcePackInfo.IFactory factory) {
        // Initialize directories
        if (!this.folder.isDirectory()) {
            this.folder.mkdirs();
        }

        // Initialize ordering file if it doesn't already exist
        if (ordering != null && !ordering.isFile()) {
            PackOrdering emptyPackOrdering = new PackOrdering(new String[]{});
            try {
                JSON.createJsonFileFromObject(ordering.toPath(), emptyPackOrdering);
            } catch (IOException e) {
                Paxi.LOGGER.error("Unable to create default pack ordering file! This shouldn't happen.");
                Paxi.LOGGER.error(e.toString());
            }
        }

        File[] packs = loadPacksFromFiles();

        if (packs != null) {
            for (File file : packs) {
                String packName = file.getName();
                ResourcePackInfo resourcePackProfile = ResourcePackInfo.createResourcePack(
                    packName,
                    true,
                    this.createResourcePack(file),
                    factory,
                    ResourcePackInfo.Priority.TOP,
                    PaxiResourcePackSource.PACK_SOURCE_PAXI);

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
                Paxi.LOGGER.error("Error loading Paxi datapack datapack_load_order.json file: {}", e.toString());
            }

            // Check that we loaded ordering properly
            if (packOrdering == null) {
                // If loading the ordering failed, we default to random ordering
                Paxi.LOGGER.error("Unable to load datapack_load_order.json! Is it proper JSON formatting? Ignoring load order...");
                return this.folder.listFiles(POSSIBLE_PACK);
            } else if (packOrdering.getOrderedPackNames() == null) {
                // User probably mistyped the "loadOrder" key - Let them know and default to random order
                Paxi.LOGGER.error("Unable to find entry with name 'loadOrder' in datapack_load_order.json! Ignoring load order...");
                return this.folder.listFiles(POSSIBLE_PACK);
            } else {
                // If loading ordering succeeded, we first load the ordered packs
                List<File> orderedPacks = filesFromNames(packOrdering.getOrderedPackNames(), POSSIBLE_PACK);

                orderedPacks.forEach(file -> this.orderedPaxiPacks.add(file.getName()));

                // Next we append any leftover packs with unspecified order
                File[] allPacks = this.folder.listFiles(POSSIBLE_PACK);
                List<File> leftoverPacks = allPacks == null
                    ? Lists.newArrayList()
                    : Arrays.stream(allPacks).filter(file -> !orderedPacks.contains(file)).collect(Collectors.toList());
                return Stream.of(orderedPacks, leftoverPacks).flatMap(Collection::stream).toArray(File[]::new);
            }
        } else {
            // If ordering file doesn't exist, load files in any order
            return this.folder.listFiles(POSSIBLE_PACK);
        }
    }

    /**
     * Creates a List of File objects created from the provided file names.
     * Each File must pass the provided filter to be added to the List.
     */
    private List<File> filesFromNames(String[] packFileNames, FileFilter filter) {
        ArrayList<File> packFiles = new ArrayList<>();

        for (String fileName : packFileNames) {
            File packFile = new File(this.folder, fileName);

            if (!packFile.exists()) {
                Paxi.LOGGER.error("Unable to find pack with name {} specified in datapack_load_order.json! Skipping...", fileName);
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
    private Supplier<IResourcePack> createResourcePack(File file) {
        return file.isDirectory()
            ? () -> new FolderPack(file)
            : () -> new FilePack(file);
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
