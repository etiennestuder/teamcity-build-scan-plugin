package nu.studer.teamcity.buildscan;

import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class BuildScanBuildServerListener extends BuildServerAdapter {

    private static final Logger LOGGER = Logger.getLogger("jetbrains.buildServer.BUILDSCAN");

    private final PluginDescriptor pluginDescriptor;
    private final SBuildServer buildServer;
    private final BuildScanDisplayArbiter buildScanDisplayArbiter;
    private final BuildScanLookup buildScanLookup;
    private final ExternalIntegration externalIntegration;

    @SuppressWarnings("WeakerAccess")
    public BuildScanBuildServerListener(
        @NotNull PluginDescriptor pluginDescriptor,
        @NotNull SBuildServer buildServer,
        @NotNull BuildScanDisplayArbiter buildScanDisplayArbiter,
        @NotNull BuildScanLookup buildScanLookup,
        @NotNull ExternalIntegration externalIntegration
    ) {
        this.pluginDescriptor = pluginDescriptor;
        this.buildServer = buildServer;
        this.buildScanDisplayArbiter = buildScanDisplayArbiter;
        this.buildScanLookup = buildScanLookup;
        this.externalIntegration = externalIntegration;
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public void register() {
        buildServer.addListener(this);

        LOGGER.info(String.format("Registered %s. %s-%s", getClass().getSimpleName(), pluginDescriptor.getPluginName(), pluginDescriptor.getPluginVersion()));
    }

    @Override
    public void buildFinished(@NotNull SRunningBuild build) {
        if (buildScanDisplayArbiter.showBuildScanInfo(build)) {
            // prepare the cache to be ready when queried by the UI
            BuildScanReferences buildScans = buildScanLookup.getBuildScansForBuild(build);

            // notify external integration points
            if (!buildScans.isEmpty()) {
                externalIntegration.handle(buildScans, build.getBuildOwnParameters());
            }
        }
    }

}
