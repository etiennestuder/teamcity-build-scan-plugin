package nu.studer.teamcity.buildscan;

import jetbrains.buildServer.serverSide.BuildPromotion;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.PositionConstraint;
import jetbrains.buildServer.web.openapi.ViewBuildTab;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public final class BuildScanViewBuildTab extends ViewBuildTab {

    private static final Logger LOGGER = Logger.getLogger("jetbrains.buildServer.BUILDSCAN");

    private final BuildScanDisplayArbiter buildScanDisplayArbiter;
    private final BuildScanLookup buildScanLookup;

    public BuildScanViewBuildTab(
        @NotNull PluginDescriptor pluginDescriptor,
        @NotNull SBuildServer buildServer,
        @NotNull PagePlaces pagePlaces,
        @NotNull BuildScanDisplayArbiter buildScanDisplayArbiter,
        @NotNull BuildScanLookup buildScanLookup
    ) {
        super("Build Scan", "buildScan", pagePlaces, pluginDescriptor.getPluginResourcesPath("/buildScanPage.jsp"), buildServer);
        setPosition(PositionConstraint.last());

        this.buildScanDisplayArbiter = buildScanDisplayArbiter;
        this.buildScanLookup = buildScanLookup;

        LOGGER.info(String.format("Registered %s. %s-%s", getClass().getSimpleName(), pluginDescriptor.getPluginName(), pluginDescriptor.getPluginVersion()));
    }

    @Override
    public boolean isAvailable(@NotNull HttpServletRequest request, @NotNull BuildPromotion promotion) {
        SBuild build = promotion.getAssociatedBuild();
        return build != null && buildScanDisplayArbiter.showBuildScanInfo(build);
    }

    @Override
    public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request, @NotNull BuildPromotion promotion) {
        //noinspection ConstantConditions
        BuildScanReferences buildScans = buildScanLookup.getBuildScansForBuild(promotion.getAssociatedBuild());
        model.put("buildScans", buildScans);
    }

}
