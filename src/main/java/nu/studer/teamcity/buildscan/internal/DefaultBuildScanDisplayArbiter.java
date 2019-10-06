package nu.studer.teamcity.buildscan.internal;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import nu.studer.teamcity.buildscan.BuildScanDisplayArbiter;
import nu.studer.teamcity.buildscan.BuildScanLookup;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

final class DefaultBuildScanDisplayArbiter implements BuildScanDisplayArbiter {

    static final String GRADLE_RUNNER = "gradle-runner";
    static final String MAVEN_RUNNER = "Maven2";
    static final String SIMPLE_RUNNER = "simpleRunner";

    private final BuildScanLookup buildScanLookup;

    DefaultBuildScanDisplayArbiter(@NotNull BuildScanLookup buildScanLookup) {
        this.buildScanLookup = buildScanLookup;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean showBuildScanInfo(@NotNull SBuild build) {
        SBuildType buildType = build.getBuildType();

        // note that buildType.getBuildRunners() returns all configured runners while
        // buildType.getRunnerTypes() returns only the runners that are enabled,
        // since the buildType instance always reflects the _current_ configuration rather
        // than the configuration at the time the build was run, we need to be defensive here and
        // show the info even when the runner could have been disabled in the given build
        List<String> runnerTypes = buildType == null ? Collections.emptyList() : buildType.getBuildRunners()
            .stream()
            .map(r -> r.getRunType().getType())
            .collect(Collectors.toList());

        if (runnerTypes.contains(GRADLE_RUNNER)) {
            return true;
        } else if (runnerTypes.contains(MAVEN_RUNNER)) {
            return true;
        } else if (runnerTypes.contains(SIMPLE_RUNNER) && !buildScanLookup.getBuildScansForBuild(build).isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

}
