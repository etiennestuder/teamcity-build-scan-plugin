package nu.studer.teamcity.buildscan.internal;

import jetbrains.buildServer.serverSide.SBuild;
import nu.studer.teamcity.buildscan.BuildScanReference;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface BuildScanDataStore {

    /**
     * Stores an empty data set for the given build. Calls to {@link #fetch} differentiate between returning an empty
     * result set and {@code null} for a build for which no information exists. Calls to this method will store an
     * empty entry in the store for the given build resulting in subsequent calls to {@link #fetch} returning an
     * empty collection instead of {@code null}. Calls to {@link #store} can be made after calling this method to store
     * subsequent data.
     *
     * Calls to this method after calling {@link #fetch} for the same build are ignored. Existing data for the
     * build will remain unchanged.
     *
     * @param build the build for which no scans have yet been published
     */
    void mark(SBuild build);

    /**
     * Stores the given build scan URL and associates with the given build. This method can be called multiple times
     * with the same build to store more than one build scan URL.
     *
     * @param build the build that published the build scan
     * @param buildScanUrl the URL of the published build scan
     */
    void store(SBuild build, String buildScanUrl);

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
