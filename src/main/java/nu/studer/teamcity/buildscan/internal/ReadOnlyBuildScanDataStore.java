package nu.studer.teamcity.buildscan.internal;

import jetbrains.buildServer.serverSide.SBuild;
import nu.studer.teamcity.buildscan.BuildScanReference;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ReadOnlyBuildScanDataStore {

    /**
     * Returns the list of all build scans published by the given build. If the build published no scans an empty list
     * is returned or {@code null} if no data exists for the requested build.
     *
     * @param build the requested build
     * @return all published scans for the given build or {@code null} if no data exists for the given build
     */
    @Nullable
    List<BuildScanReference> fetch(SBuild build);

}
