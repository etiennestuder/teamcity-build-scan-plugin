package nu.studer.teamcity.buildscan;

import jetbrains.buildServer.controllers.BuildDataExtensionUtil;
import jetbrains.buildServer.serverSide.BuildPromotion;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.PositionConstraint;
import jetbrains.buildServer.web.openapi.SimplePageExtension;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static jetbrains.buildServer.web.openapi.ViewBuildTab.BUILD_PROMOTION_KEY;

public final class BuildScanCrumbSummaryExtension extends SimplePageExtension {

    private static final Logger LOGGER = Logger.getLogger("jetbrains.buildServer.BUILDSCAN");

    private final SBuildServer buildServer;
    private final BuildScanDisplayArbiter buildScanDisplayArbiter;
    private final BuildScanLookup buildScanLookup;

    public BuildScanCrumbSummaryExtension(
        @NotNull PagePlaces pagePlaces,
        @NotNull PluginDescriptor pluginDescriptor,
        @NotNull SBuildServer buildServer,
        @NotNull BuildScanDisplayArbiter buildScanDisplayArbiter,
        @NotNull BuildScanLookup buildScanLookup
    ) {
        super(pagePlaces, PlaceId.BUILD_SUMMARY, "buildScan", pluginDescriptor.getPluginResourcesPath("buildScanCrumbSummary.jsp"));
        setPosition(PositionConstraint.last());

        this.buildServer = buildServer;
        this.buildScanDisplayArbiter = buildScanDisplayArbiter;
        this.buildScanLookup = buildScanLookup;

        LOGGER.info(String.format("Registered %s. %s-%s", getClass().getSimpleName(), pluginDescriptor.getPluginName(), pluginDescriptor.getPluginVersion()));
    }

    @Override
    public boolean isAvailable(@NotNull HttpServletRequest request) {
        BuildPromotion promotion = getPromotion(request);
        return promotion != null && isAvailable(promotion);
    }

    private boolean isAvailable(@NotNull BuildPromotion promotion) {
        SBuild build = promotion.getAssociatedBuild();
        return build != null && buildScanDisplayArbiter.showBuildScanInfo(build);
    }

    @Override
    public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) {
        BuildPromotion promotion = getPromotion(request);
        //noinspection ConstantConditions
        BuildScanReferences buildScans = buildScanLookup.getBuildScansForBuild(promotion.getAssociatedBuild());
        model.put("buildScans", buildScans);
    }

    @Nullable
    private BuildPromotion getPromotion(@NotNull HttpServletRequest request) {
        BuildPromotion promotion = (BuildPromotion) request.getAttribute(BUILD_PROMOTION_KEY);
        if (promotion != null) {
            return promotion;
        } else {
            SBuild build = BuildDataExtensionUtil.retrieveBuild(request, buildServer);
            return build != null ? build.getBuildPromotion() : null;
        }
    }

}
