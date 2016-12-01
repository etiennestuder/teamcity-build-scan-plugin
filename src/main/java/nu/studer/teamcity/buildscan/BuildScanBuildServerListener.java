package nu.studer.teamcity.buildscan;

import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.net.URL;

public final class BuildScanBuildServerListener extends BuildServerAdapter {

    private static final Logger LOGGER = Logger.getLogger("jetbrains.buildServer.BUILDSCAN");

    private final PluginDescriptor pluginDescriptor;
    private final SBuildServer buildServer;
    private final BuildScanDisplayArbiter buildScanDisplayArbiter;
    private final BuildScanLookup buildScanLookup;

    @SuppressWarnings("WeakerAccess")
    public BuildScanBuildServerListener(
        @NotNull PluginDescriptor pluginDescriptor,
        @NotNull SBuildServer buildServer,
        @NotNull BuildScanDisplayArbiter buildScanDisplayArbiter,
        @NotNull BuildScanLookup buildScanLookup
    ) {
        this.pluginDescriptor = pluginDescriptor;
        this.buildServer = buildServer;
        this.buildScanDisplayArbiter = buildScanDisplayArbiter;
        this.buildScanLookup = buildScanLookup;
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

            // notify Slack web hook, todo: do in parallel
            URL webhookUrl = Util.toUrl("https://hooks.slack.com/services/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");
            new SlackIntegration(webhookUrl).notify(buildScans);
        }
    }

}
