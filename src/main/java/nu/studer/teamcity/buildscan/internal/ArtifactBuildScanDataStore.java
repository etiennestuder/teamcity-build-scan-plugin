package nu.studer.teamcity.buildscan.internal;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import jetbrains.buildServer.ArtifactsConstants;
import jetbrains.buildServer.serverSide.SBuild;
import nu.studer.teamcity.buildscan.BuildScanDataStore;
import nu.studer.teamcity.buildscan.BuildScanReference;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class ArtifactBuildScanDataStore implements BuildScanDataStore {

    private static final Logger LOGGER = Logger.getLogger("jetbrains.buildServer.BUILDSCAN");

    // storage related coordinates
    private static final String BUILD_SCANS_DIR = "build_scans";
    private static final String BUILD_SCAN_LINKS_FILE = "build_scans.txt";

    private final BuildScanDataStore delegate;

    public ArtifactBuildScanDataStore(@NotNull BuildScanDataStore delegate) {
        this.delegate = delegate;
    }

    @Override
    public void mark(SBuild build) {
        final Path buildScanFile = getBuildScanFile(build.getArtifactsDirectory());
        try {
            createFileIfNotExists(buildScanFile);
        } catch (IOException ex) {
            LOGGER.error(String.format("Could not create buildscan file %s", buildScanFile), ex);
        }
    }

    @Override
    public void store(SBuild build, String buildScanUrl) {
        final Path buildScanFile = getBuildScanFile(build.getArtifactsDirectory());
        try {
            createFileIfNotExists(buildScanFile);
            Files.write(buildScanFile, Collections.singletonList(buildScanUrl), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
            LOGGER.debug(String.format("Successfully stored buildscan url %s for build %s in directory %s", buildScanUrl, build.getBuildId(), build.getArtifactsDirectory()));
        } catch (IOException ex) {
            LOGGER.error(String.format("Could not store build scan url %s into buildscan file %s", buildScanUrl, buildScanFile), ex);
        }
    }

    @Override
    public List<BuildScanReference> fetch(SBuild build) {
        final Path buildScanFile = getBuildScanFile(build.getArtifactsDirectory());
        List<BuildScanReference> result;
        if (Files.exists(buildScanFile)) {
            try {
                result = Files.lines(buildScanFile, Charsets.UTF_8).map(scanUrl -> new BuildScanReference(Util.getBuildScanId(scanUrl), scanUrl)).collect(Collectors.toList());
            } catch (IOException ex) {
                LOGGER.error(String.format("Could not read buildscan file %s", buildScanFile), ex);
                result = Collections.emptyList();
            }
        } else {
            return delegate.fetch(build);
        }
        return result;
    }

    @NotNull
    @VisibleForTesting
    Path getBuildScanFile(File directory) {
        return Paths.get(directory.getAbsolutePath(), ArtifactsConstants.TEAMCITY_ARTIFACTS_DIR, BUILD_SCANS_DIR, BUILD_SCAN_LINKS_FILE);
    }

    private static void createFileIfNotExists(Path file) throws IOException {
        if (!Files.exists(file)) {
            Files.createDirectories(file.getParent());
            try {
                Files.createFile(file);
            } catch (FileAlreadyExistsException e) {
                // not a failure scenario, this can happen since our check for existence and
                // the possible creation are not atomic (while createFile _is_ atomic)
            }
        }
    }

}
