package com.yungnickyoung.minecraft.paxi;

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
import org.jetbrains.annotations.Nullable;

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
        if (this.orderingFile != null && !this.orderingFile.isFile()) {
            PackOrdering emptyPackOrdering = new PackOrdering(new String[]{});
            try {
                JSON.createJsonFileFromObject(this.orderingFile.toPath(), emptyPackOrdering);
            } catch (IOException e) {
                PaxiCommon.LOGGER.error("Unable to create default pack ordering file! This shouldn't happen.");
                PaxiCommon.LOGGER.error(e.toString());
            }
        }

        List<Path> packPathsToLoad = toPaths(loadPacksFromFiles());

        for (Path packPath : packPathsToLoad) {
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
     * If this repository source has an orderingFile defined, the returned list will contain the specified Files
     * in the proper order, with any unordered Files prepended to the start of the list.
     * <p>
     * If this repository source does not have an orderingFile defined, the returned list's items have no guaranteed order.
     */
    private List<File> loadPacksFromFiles() {
        // Reset ordered and unordered pack lists
        this.orderedPaxiPacks.clear();
        this.unorderedPaxiPacks.clear();

        // Begin creating a list of all Paxi packs (excluding external packs referenced in the ordering json)
        List<File> allPacks = new ArrayList<>();

        // NEW FEATURE - add files in the base 'datapacks' folder (i.e. <minecraft>/datapacks)
        // This is to support CurseForge's new data pack system
        if (PaxiCommon.CONFIG.loadFromBaseDatapacksDirectory && ((FolderRepositorySourceAccessor) this).getPackType() == PackType.SERVER_DATA) {
            File basePacksFolder = new File(PaxiCommon.BASE_GAME_DIRECTORY, "datapacks");
            if (!basePacksFolder.exists()) {
                basePacksFolder.mkdirs();
            }
            File[] basePacks = basePacksFolder.listFiles(PACK_FILTER);
            if (basePacks != null) {
                allPacks.addAll(Arrays.asList(basePacks));
            }
        }

        // Next we add packs in the Paxi folder (i.e. <minecraft>/config/paxi/datapacks)
        // This is the classic Paxi behavior
        File[] paxiPacks = ((FolderRepositorySourceAccessor) this).getFolder().toFile().listFiles(PACK_FILTER);
        if (paxiPacks != null) {
            allPacks.addAll(Arrays.asList(paxiPacks));
        }

        if (this.orderingFile != null) {
            // If ordering file exists, load any specified files in the specific order
            PackOrdering packOrdering = null;
            try {
                packOrdering = JSON.loadObjectFromJsonFile(this.orderingFile.toPath(), PackOrdering.class);
            } catch (IOException | JsonIOException | JsonSyntaxException e) {
                PaxiCommon.LOGGER.error("Error loading Paxi ordering JSON file {}: {}", this.orderingFile.getName(), e.toString());
            }

            // Check that we loaded ordering properly
            if (packOrdering == null) {
                // If loading the ordering failed, we default to random ordering
                PaxiCommon.LOGGER.error("Unable to load ordering JSON file {}! Is it proper JSON formatting? Ignoring load order...", this.orderingFile.getName());
                allPacks.forEach(file -> this.unorderedPaxiPacks.add(file.getName()));
                return allPacks;
            } else if (packOrdering.getOrderedPackNames() == null) {
                // User probably mistyped the "loadOrder" key - Let them know and default to random order
                PaxiCommon.LOGGER.error("Unable to find entry with name 'loadOrder' in load ordering JSON file {}! Ignoring load order...", this.orderingFile.getName());
                allPacks.forEach(file -> this.unorderedPaxiPacks.add(file.getName()));
                return allPacks;
            } else {
                // If loading ordering succeeded, we add the ordered packs
                List<File> orderedPacks = filesFromNames(packOrdering.getOrderedPackNames(), PACK_FILTER);
                List<File> unorderedPacks = allPacks.stream().filter(file -> !orderedPacks.contains(file)).toList();
                orderedPacks.forEach(file -> this.orderedPaxiPacks.add(file.getName()));
                unorderedPacks.forEach(file -> this.unorderedPaxiPacks.add(file.getName()));
                return Stream.of(unorderedPacks, orderedPacks).flatMap(Collection::stream).toList();
            }
        } else {
            // If ordering file doesn't exist, load files in any order
            allPacks.forEach(file -> this.unorderedPaxiPacks.add(file.getName()));
            return allPacks;
        }
    }

    /**
     * Creates a List of File objects created from the provided file names.
     * Each File must pass the provided filter to be added to the List.
     */
    private List<File> filesFromNames(String[] packFileNames, @Nullable FileFilter filter) {
        ArrayList<File> packFiles = new ArrayList<>();

        for (String fileName : packFileNames) {
            // First, check for the pack as-is, using the base Minecraft folder as the base directory
            File packFile = new File(PaxiCommon.BASE_GAME_DIRECTORY, fileName);

            if (!packFile.exists()) {
                // If the pack doesn't exist, check for it in the Paxi datapacks/resourcepacks directory.
                // This is the base Paxi behavior.
                packFile = new File(((FolderRepositorySourceAccessor) this).getFolder().toFile().toString(), fileName);
            }

            if (!packFile.exists()) {
                // If the pack file still doesn't exist, log an error and skip it
                PaxiCommon.LOGGER.error("Unable to find pack with name {} specified in load ordering JSON file {}! Skipping...", fileName, this.orderingFile.getName());
            } else if (filter != null && !filter.accept(packFile)) {
                // If the pack file doesn't pass the filter, log an error and skip it
                PaxiCommon.LOGGER.error("Attempted to load pack {} but it is not a valid pack format! It may be missing a pack.mcmeta file. Skipping...", fileName);
            } else {
                // If the pack file exists and passes the filter, add it to the list
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

    private static List<Path> toPaths(List<File> files) {
        return files.stream().map(File::toPath).toList();
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
