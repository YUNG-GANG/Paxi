package com.yungnickyoung.minecraft.paxi;

import com.google.common.collect.Lists;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.yungnickyoung.minecraft.paxi.mixin.accessor.FolderRepositorySourceAccessor;
import com.yungnickyoung.minecraft.yungsapi.io.JSON;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.Pack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
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

    private final File orderingFile;
    public List<String> orderedPaxiPacks = new ArrayList<>();
    public List<String> unorderedPaxiPacks = new ArrayList<>();

    public PaxiRepositorySource(Path packsFolder, PackType packType, File orderingFile) {
        super(packsFolder, packType, PaxiPackSource.PACK_SOURCE_PAXI, null);
        this.orderingFile = orderingFile;
    }

    @Override
    public void loadPacks(@NotNull Consumer<Pack> packAdder) {
        // Initialize directories
        File folder = ((FolderRepositorySourceAccessor) this).getFolder().toFile();
        if (!folder.isDirectory()) {
            folder.mkdirs();
        }

        // Initialize ordering file if it doesn't already exist
        if (orderingFile != null && !orderingFile.isFile()) {
            PackOrdering emptyPackOrdering = new PackOrdering(new String[]{});
            try {
                JSON.createJsonFileFromObject(orderingFile.toPath(), emptyPackOrdering);
            } catch (IOException e) {
                PaxiCommon.LOGGER.error("Unable to create default pack ordering file! This shouldn't happen.");
                PaxiCommon.LOGGER.error(e.toString());
            }
        }

        Path[] packs = loadPacksFromFiles();

        for (Path packPath : packs) {
            String packName = packPath.getFileName().toString();
            PackLocationInfo packLocationInfo = new PackLocationInfo(packName, Component.literal(packName), PaxiPackSource.PACK_SOURCE_PAXI, Optional.empty());
            PackSelectionConfig packSelectionConfig = new PackSelectionConfig(true, Pack.Position.TOP, false);

            Pack pack = Pack.readMetaAndCreate(
                    packLocationInfo,
                    this.createPackResourcesSupplier(packPath),
                    ((FolderRepositorySourceAccessor) this).getPackType(),
                    packSelectionConfig
            );

            if (pack != null) {
                packAdder.accept(pack);
            }
        }
    }

    /**
     * Builds an array of Files corresponding to the valid packs in this object's packsFolder.
     * <p>
     * If this pack provider has an ordering File defined, the returned array will contained the specified Files
     * in the proper order, with any unspecified Files appended to the end of the List.
     * <p>
     * If this pack provider does not have an ordering File defined, the returned array's items have no guaranteed order.
     */
    private Path[] loadPacksFromFiles() {
        // Reset ordered and unordered pack lists
        this.orderedPaxiPacks.clear();
        this.unorderedPaxiPacks.clear();

        if (this.orderingFile != null) {
            // If ordering file exists, load any specified files in the specific order
            PackOrdering packOrdering = null;
            try {
                packOrdering = JSON.loadObjectFromJsonFile(orderingFile.toPath(), PackOrdering.class);
            } catch (IOException | JsonIOException | JsonSyntaxException e) {
                PaxiCommon.LOGGER.error("Error loading Paxi ordering JSON file {}: {}", this.orderingFile.getName(), e.toString());
            }

            // Check that we loaded ordering properly
            if (packOrdering == null) {
                // If loading the ordering failed, we default to random ordering
                PaxiCommon.LOGGER.error("Unable to load ordering JSON file {}! Is it proper JSON formatting? Ignoring load order...", this.orderingFile.getName());
                File[] files = ((FolderRepositorySourceAccessor) this).getFolder().toFile().listFiles(PACK_FILTER);
                return toPaths(files);

            } else if (packOrdering.getOrderedPackNames() == null) {
                // User probably mistyped the "loadOrder" key - Let them know and default to random order
                PaxiCommon.LOGGER.error("Unable to find entry with name 'loadOrder' in load ordering JSON file {}! Ignoring load order...", this.orderingFile.getName());
                File[] files = ((FolderRepositorySourceAccessor) this).getFolder().toFile().listFiles(PACK_FILTER);
                return toPaths(files);
            } else {
                // If loading ordering succeeded, we first load the ordered packs
                List<File> orderedPacks = filesFromNames(packOrdering.getOrderedPackNames(), PACK_FILTER);

                // Next we prepend any leftover packs with unspecified order
                File[] allPacks = ((FolderRepositorySourceAccessor) this).getFolder().toFile().listFiles(PACK_FILTER);
                List<File> unorderedPacks = allPacks == null
                        ? Lists.newArrayList()
                        : Arrays.stream(allPacks).filter(file -> !orderedPacks.contains(file)).collect(Collectors.toList());

                orderedPacks.forEach(file -> this.orderedPaxiPacks.add(file.getName()));
                unorderedPacks.forEach(file -> this.unorderedPaxiPacks.add(file.getName()));

                File[] files = Stream.of(unorderedPacks, orderedPacks).flatMap(Collection::stream).toArray(File[]::new);
                return toPaths(files);
            }
        } else {
            // If ordering file doesn't exist, load files in any order
            File[] files = ((FolderRepositorySourceAccessor) this).getFolder().toFile().listFiles(PACK_FILTER);
            return toPaths(files);
        }
    }

    /**
     * Creates a List of File objects created from the provided file names.
     * Each File must pass the provided filter to be added to the List.
     */
    private List<File> filesFromNames(String[] packFileNames, FileFilter filter) {
        ArrayList<File> packFiles = new ArrayList<>();

        for (String fileName : packFileNames) {
            File packFile = new File(((FolderRepositorySourceAccessor) this).getFolder().toFile().toString(), fileName);

            if (!packFile.exists()) {
                PaxiCommon.LOGGER.error("Unable to find pack with name {} specified in load ordering JSON file {}! Skipping...", fileName, this.orderingFile.getName());
            } else if ((filter == null) || filter.accept(packFile)) {
                packFiles.add(packFile);
            }
        }
        return packFiles;
    }

    /**
     * Creates the proper ResourcePack supplier for the given file.
     * Assumes that the provided file has already been validated as a properly formatted zip or folder pack.
     */
    private Pack.ResourcesSupplier createPackResourcesSupplier(Path path) {
        File file = path.toFile();

        // If the file is a zip, we use FilePackResources
        if (file.isFile() && file.getName().endsWith(".zip")) {
            return new FilePackResources.FileResourcesSupplier(path);
        }

        // If the file is a folder, we use PathPackResources
        if (file.isDirectory() && (new File(file, "pack.mcmeta")).isFile()) {
            return new PathPackResources.PathResourcesSupplier(path);
        }

        // If the file is neither a zip nor a folder, we throw an exception
        throw new IllegalArgumentException("Invalid Paxi pack file: " + file);
    }

    private static Path[] toPaths(File[] files) {
        if (files == null) {
            return new Path[]{};
        }
        return Arrays.stream(files).map(File::toPath).toArray(Path[]::new);
    }

    public boolean hasPacks() {
        return !this.unorderedPaxiPacks.isEmpty() || !this.orderedPaxiPacks.isEmpty();
    }

    /**
     * Class for JSON load order serialization.
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
