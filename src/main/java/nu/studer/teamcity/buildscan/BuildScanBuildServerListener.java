package nu.studer.teamcity.buildscan;

import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class BuildScanBuildServerListener extends BuildServerAdapter {

    private static final Logger LOGGER = Logger.getLogger("jetbrains.buildServer.BUILDSCAN");
    private static final String GRADLE_RUNNER = "gradle-runner";
    private static final String MAVEN_RUNNER = "Maven2";
    private static final String COMMAND_LINE_RUNNER = "simpleRunner";

    private final PluginDescriptor pluginDescriptor;
    private final SBuildServer buildServer;
    private final BuildScanDisplayArbiter buildScanDisplayArbiter;
    private final BuildScanLookup buildScanLookup;
    private final ExternalIntegration externalIntegration;
    private final BuildScanDataStore buildScanDataStore;

    @SuppressWarnings("WeakerAccess")
    public BuildScanBuildServerListener(
        @NotNull PluginDescriptor pluginDescriptor,
        @NotNull SBuildServer buildServer,
        @NotNull BuildScanDisplayArbiter buildScanDisplayArbiter,
        @NotNull BuildScanLookup buildScanLookup,
        @NotNull ExternalIntegration externalIntegration,
        @NotNull BuildScanDataStore buildScanDataStore
    ) {
        this.pluginDescriptor = pluginDescriptor;
        this.buildServer = buildServer;
        this.buildScanDisplayArbiter = buildScanDisplayArbiter;
        this.buildScanLookup = buildScanLookup;
        this.externalIntegration = externalIntegration;
        this.buildScanDataStore = buildScanDataStore;
    }

    @SuppressWarnings("unused")
    public void register() {
        buildServer.addListener(this);
        LOGGER.info(String.format("Registered %s. %s-%s", getClass().getSimpleName(), pluginDescriptor.getPluginName(), pluginDescriptor.getPluginVersion()));
    }

    @SuppressWarnings("unused")
    public void unregister() {
        buildServer.removeListener(this);
        LOGGER.info(String.format("Unregistered %s. %s-%s", getClass().getSimpleName(), pluginDescriptor.getPluginName(), pluginDescriptor.getPluginVersion()));
    }

    @Override
    public void buildStarted(@NotNull SRunningBuild build) {
        if (build.getBuildType() != null) {
            Collection<String> runnerTypes = build.getBuildType().getRunnerTypes();
            if (runnerTypes.contains(GRADLE_RUNNER) || runnerTypes.contains(MAVEN_RUNNER) || runnerTypes.contains(COMMAND_LINE_RUNNER)) {
                buildScanDataStore.mark(build);
            }
        }
    }

    @Override
    public void buildFinished(@NotNull SRunningBuild build) {
        if (buildScanDisplayArbiter.showBuildScanInfo(build)) {
            // prepare the cache to be ready when queried by the UI
            BuildScanReferences buildScans = buildScanLookup.getBuildScansForBuild(build);

            // notify external integration points
            if (!buildScans.isEmpty()) {
                TeamCityBuildStatus teamCityBuildStatus = TeamCityBuildStatus.from(build.getBuildStatus());
                TeamCityConfiguration teamCityConfiguration = new TeamCityConfiguration(build.getFullName(), build.getParametersProvider().getAll());
                externalIntegration.handle(buildScans, teamCityBuildStatus, teamCityConfiguration);
            }
        }
    }

}
