package nu.studer.teamcity.buildscan.internal;

import jetbrains.buildServer.serverSide.SBuild;
import nu.studer.teamcity.buildscan.BuildScanReference;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class ArtifactBuildScanDataStore implements BuildScanDataStore {

    private final BuildScanDataStore delegate;

    public ArtifactBuildScanDataStore(@NotNull BuildScanDataStore delegate) {
        this.delegate = delegate;
    }

    @Override
    public void mark(SBuild build) {
        this.delegate.mark(build);
    }

    @Override
    public void store(SBuild build, String buildScanUrl) {
        this.delegate.store(build, buildScanUrl);
    }

    @Override
    public List<BuildScanReference> fetch(SBuild build) {
        return this.delegate.fetch(build);
    }

}
