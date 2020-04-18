package nu.studer.teamcity.buildscan.internal;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import jetbrains.buildServer.ArtifactsConstants;
import jetbrains.buildServer.serverSide.SBuild;
import nu.studer.teamcity.buildscan.BuildScanDataStore;
import nu.studer.teamcity.buildscan.BuildScanReference;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

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
        final Path buildScanLinksFile = getBuildScanLinksFile(build);

        try {
            createFileIfNotExists(buildScanLinksFile);
        } catch (IOException e) {
            LOGGER.error(String.format("Could not create build scan links file %s", buildScanLinksFile), e);
        }
    }

    @Override
    public void store(SBuild build, String buildScanUrl) {
        final Path buildScanLinksFile = getBuildScanLinksFile(build);

        try {
            createFileIfNotExists(buildScanLinksFile);
        } catch (IOException e) {
            LOGGER.error(String.format("Could not create build scan links file %s", buildScanLinksFile), e);
        }

        try {
            Files.write(buildScanLinksFile, Collections.singletonList(buildScanUrl), StandardOpenOption.WRITE, StandardOpenOption.APPEND);
            LOGGER.info("Persisting build scan URL: " + buildScanUrl + ", for build id: " + build.getBuildId());
        } catch (IOException e) {
            LOGGER.error(String.format("Could not store build scan URL %s in build scan links file %s", buildScanUrl, buildScanLinksFile), e);
        }
    }

    @Override
    public List<BuildScanReference> fetch(SBuild build) {
        final Path buildScanLinksFile = getBuildScanLinksFile(build);

        List<BuildScanReference> result;
        if (Files.exists(buildScanLinksFile)) {
            try {
                result = Files.lines(buildScanLinksFile, Charsets.UTF_8).map(scanUrl -> new BuildScanReference(Util.getBuildScanId(scanUrl), scanUrl)).collect(Collectors.toList());
            } catch (IOException e) {
                LOGGER.error(String.format("Could not read buildscan file %s", buildScanLinksFile), e);
                result = Collections.emptyList();
            }
        } else {
            return delegate.fetch(build);
        }

        return result;
    }

    @NotNull
    @VisibleForTesting
    Path getBuildScanLinksFile(SBuild build) {
        return Paths.get(build.getArtifactsDirectory().getAbsolutePath(), ArtifactsConstants.TEAMCITY_ARTIFACTS_DIR, BUILD_SCANS_DIR, BUILD_SCAN_LINKS_FILE);
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
