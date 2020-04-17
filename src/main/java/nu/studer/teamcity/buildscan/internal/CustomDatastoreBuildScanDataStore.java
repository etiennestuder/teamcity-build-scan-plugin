package nu.studer.teamcity.buildscan.internal;

import com.google.common.collect.Maps;
import jetbrains.buildServer.Build;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.util.StringUtil;
import nu.studer.teamcity.buildscan.BuildScanReference;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;


public final class CustomDatastoreBuildScanDataStore extends BuildServerAdapter implements ReadOnlyBuildScanDataStore {

    private static final String BUILD_SCAN_STORAGE_ID = "nu.studer.teamcity.buildscan.DefaultBuildScanDataStore";
    private static final String BUILD_SCAN_URLS_SEPARATOR = "|";
    private final SBuildServer server;

    public CustomDatastoreBuildScanDataStore(SBuildServer buildServer) {
        server = buildServer;
    }

    @SuppressWarnings("unused")
    public void register() {
        server.addListener(this);
    }

    @Override
    public List<BuildScanReference> fetch(SBuild build) {
        String buildId = String.valueOf(build.getBuildId());
        //noinspection ConstantConditions
        CustomDataStorage customDataStorage = getCustomDataStorage(build.getBuildType());
        String existing = customDataStorage.getValue(buildId);

        if (existing == null) {
            return null;
        } else {
            List<BuildScanReference> buildScanReferences = new ArrayList<>();
            List<String> scans = StringUtil.split(existing, BUILD_SCAN_URLS_SEPARATOR);
            for (String scanUrl : scans) {
                if (!scanUrl.isEmpty()) {
                    buildScanReferences.add(new BuildScanReference(Util.getBuildScanId(scanUrl), scanUrl));
                }
            }
            return buildScanReferences;
        }
    }

    @NotNull
    private CustomDataStorage getCustomDataStorage(SBuildType buildType) {
        return buildType.getCustomDataStorage(BUILD_SCAN_STORAGE_ID);
    }

    @Override
    public void entriesDeleted(@NotNull Collection<SFinishedBuild> removedEntries) {
        for (SFinishedBuild removedEntry : removedEntries) {
            //noinspection ConstantConditions
            final CustomDataStorage customDataStorage = getCustomDataStorage(removedEntry.getBuildType());
            cleanupDeletedValues(customDataStorage);
        }
    }

    @Override
    public void cleanupFinished() {
        final List<SBuildType> activeBuildTypes = server.getProjectManager().getActiveBuildTypes();
        for (SBuildType activeBuildType : activeBuildTypes) {
            final CustomDataStorage customDataStorage = getCustomDataStorage(activeBuildType);
            cleanupDeletedValues(customDataStorage);
        }
    }

    private void cleanupDeletedValues(CustomDataStorage customDataStorage) {
        final Map<String, String> buildScans = customDataStorage.getValues();
        if (buildScans != null && !buildScans.isEmpty()) {
            List<Long> scanBuildIds = buildScans.keySet().stream().map(Long::parseLong).collect(Collectors.toList());
            final Collection<SFinishedBuild> availableBuilds = server.getHistory().findEntries(scanBuildIds);
            final Set<Long> availableBuildIds = availableBuilds.stream().map(Build::getBuildId).collect(Collectors.toSet());
            final Map<String, String> availableBuildsOnly = Maps.filterEntries(buildScans, input -> input != null && availableBuildIds.contains(Long.parseLong(input.getKey())));
            customDataStorage.putValues(availableBuildsOnly);
        }
    }
}
