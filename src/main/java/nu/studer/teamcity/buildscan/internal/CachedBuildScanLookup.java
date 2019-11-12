package nu.studer.teamcity.buildscan.internal;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.util.cache.CacheProvider;
import jetbrains.buildServer.util.cache.SCache;
import nu.studer.teamcity.buildscan.BuildScanLookup;
import nu.studer.teamcity.buildscan.BuildScanReferences;
import org.jetbrains.annotations.NotNull;

public final class CachedBuildScanLookup implements BuildScanLookup {

    private final SCache<BuildScanReferences> cache;
    private final BuildScanLookup delegate;

    public CachedBuildScanLookup(
        @NotNull CacheProvider cacheProvider,
        @NotNull BuildScanLookup delegate
    ) {
        this.cache = cacheProvider.getOrCreateCache("buildScans-0.1", BuildScanReferences.class);
        this.cache.setAllowToReset(true);
        this.delegate = delegate;
    }

    @Override
    @NotNull
    public BuildScanReferences getBuildScansForBuild(@NotNull SBuild build) {
        // do not cache the result while the build is still running, since (more) build scans might appear in the log later on
        return build.isFinished()
            ? cache.fetch(key(build), () -> delegate.getBuildScansForBuild(build))
            : delegate.getBuildScansForBuild(build);
    }

    @SuppressWarnings("unused")
    public void destroy() {
        cache.dispose();
    }

    @NotNull
    private static String key(@NotNull SBuild build) {
        return String.valueOf(build.getBuildId());
    }

}
