package nu.studer.teamcity.buildscan.internal;

import com.google.common.base.Charsets;
import jetbrains.buildServer.ArtifactsConstants;
import jetbrains.buildServer.serverSide.SBuild;
import nu.studer.teamcity.buildscan.BuildScanReference;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class ArtifactBuildScanDataStore implements BuildScanDataStore {

    private static final Logger LOGGER = Logger.getLogger("jetbrains.buildServer.BUILDSCAN");
    private final ReadOnlyBuildScanDataStore fallbackDataStore;

    public ArtifactBuildScanDataStore(ReadOnlyBuildScanDataStore fallbackDataStore) {
        this.fallbackDataStore = fallbackDataStore;
    }

    @Override
    public void mark(SBuild build) {
        final Path buildScanFile = getBuildScanFile(build);
        try {
            createIfNotExists(buildScanFile);
        } catch (IOException ex) {
            LOGGER.error(String.format("Could not create buildscan file %s", buildScanFile), ex);
        }
    }

    @Override
    public void store(SBuild build, String buildScanUrl) {
        final Path buildScanFile = getBuildScanFile(build);
        try {
            createIfNotExists(buildScanFile);
            Files.write(buildScanFile, Collections.singletonList(buildScanUrl), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
            LOGGER.debug(String.format("Successfully stored buildscan url %s for build %s in directory %s", buildScanUrl, build.getBuildId(), build.getArtifactsDirectory()));
        } catch (IOException ex) {
            LOGGER.error(String.format("Could not store build scan url %s into buildscan file %s", buildScanUrl, buildScanFile), ex);
        }
    }

    private void createIfNotExists(Path buildScanFile) throws IOException {
        Files.createDirectories(buildScanFile.getParent());
        if (!Files.exists(buildScanFile)) {
            Files.createFile(buildScanFile);
        }
    }

    @NotNull
    private Path getBuildScanFile(SBuild build) {
        return Paths.get(build.getArtifactsDirectory().getAbsolutePath(), ArtifactsConstants.TEAMCITY_ARTIFACTS_DIR, "buildscans", "buildscan_urls.txt");
    }

    @Override
    public List<BuildScanReference> fetch(SBuild build) {
        return fetchOrFallback(build);
    }

    @Nullable
    private List<BuildScanReference> fetchOrFallback(SBuild build) {
        final Path buildScanFile = getBuildScanFile(build);
        List<BuildScanReference> result;
        if (Files.exists(buildScanFile)) {
            try {
                result = Files.lines(buildScanFile, Charsets.UTF_8).map(scanUrl -> new BuildScanReference(Util.getBuildScanId(scanUrl), scanUrl)).collect(Collectors.toList());
            } catch (IOException ex) {
                LOGGER.error(String.format("Could not read buildscan file %s", buildScanFile), ex);
                result = Collections.emptyList();
            }
        } else {
            return fallbackDataStore.fetch(build);
        }
        return result;
    }
}
