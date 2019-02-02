package nu.studer.teamcity.buildscan.internal;

import jetbrains.buildServer.serverSide.SBuild;
import nu.studer.teamcity.buildscan.BuildScanLookup;
import nu.studer.teamcity.buildscan.BuildScanReference;
import nu.studer.teamcity.buildscan.BuildScanReferences;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DataStoreBuildScanLookup implements BuildScanLookup {

    private final BuildScanLookup delegate;
    private final BuildScanDataStore buildScanDataStore;

    public DataStoreBuildScanLookup(@NotNull BuildScanLookup delegate, @NotNull BuildScanDataStore buildScanDataStore) {
        this.delegate = delegate;
        this.buildScanDataStore = buildScanDataStore;
    }

    @NotNull
    @Override
    public BuildScanReferences getBuildScansForBuild(@NotNull SBuild build) {
        List<BuildScanReference> buildScanReferences = buildScanDataStore.fetch(build);

        if (buildScanReferences == null) {
            return delegate.getBuildScansForBuild(build);
        }

        return BuildScanReferences.of(buildScanReferences);
    }
}
