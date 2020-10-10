package nu.studer.teamcity.buildscan.internal;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import nu.studer.teamcity.buildscan.BuildScanDisplayArbiter;
import nu.studer.teamcity.buildscan.BuildScanLookup;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public final class DefaultBuildScanDisplayArbiter implements BuildScanDisplayArbiter {

    static final String GRADLE_RUNNER = "gradle-runner";
    static final String MAVEN_RUNNER = "Maven2";
    static final String SIMPLE_RUNNER = "simpleRunner";

    private static final List<String> BUILD_SCAN_SUPPORTING_RUNNER_TYPES = Arrays.asList(GRADLE_RUNNER, MAVEN_RUNNER, SIMPLE_RUNNER);

    private final BuildScanLookup buildScanLookup;

    public DefaultBuildScanDisplayArbiter(@NotNull BuildScanLookup buildScanLookup) {
        this.buildScanLookup = buildScanLookup;
    }

    @Override
    public boolean showBuildScanInfo(@NotNull SBuild build) {
        SBuildType buildType = build.getBuildType();
        if (buildType == null) {
            return false;
        }

        // note that buildType.getBuildRunners() returns all configured runners while
        // buildType.getRunnerTypes() returns only the runners that are enabled,
        // since the buildType instance always reflects the _current_ configuration rather
        // than the configuration at the time the build was run, we need to be defensive here and
        // show the info even when the runner could have been disabled in the given build
        long matchingRunners = buildType.getBuildRunners()
            .stream()
            .map(r -> r.getRunType().getType())
            .filter(BUILD_SCAN_SUPPORTING_RUNNER_TYPES::contains)
            .count();
        if (matchingRunners == 0) {
            return false;
        }

        return !buildScanLookup.getBuildScansForBuild(build).isEmpty();
    }

}
