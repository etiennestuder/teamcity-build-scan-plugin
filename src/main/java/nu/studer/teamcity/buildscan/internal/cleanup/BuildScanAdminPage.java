package nu.studer.teamcity.buildscan.internal.cleanup;

import jetbrains.buildServer.controllers.admin.AdminPage;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.PositionConstraint;
import nu.studer.teamcity.buildscan.internal.CustomDataStorageBuildScanCleaner;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public final class BuildScanAdminPage extends AdminPage {

    private final CustomDataStorageBuildScanCleaner customDataStorageBuildScanCleaner;

    public BuildScanAdminPage(@NotNull PagePlaces pagePlaces, @NotNull PluginDescriptor descriptor, @NotNull CustomDataStorageBuildScanCleaner customDataStorageBuildScanCleaner) {
        super(pagePlaces);
        this.customDataStorageBuildScanCleaner = customDataStorageBuildScanCleaner;
        setPluginName(descriptor.getPluginName());
        setIncludeUrl(descriptor.getPluginResourcesPath("/admin/buildScanAdminPage.jsp"));
        addJsFile(descriptor.getPluginResourcesPath("js/buildScan.js"));
        setTabTitle("Build scan");
        setPosition(PositionConstraint.last());
    }

    @NotNull
    public String getGroup() {
        return SERVER_RELATED_GROUP;
    }

    @Override
    public boolean isAvailable(@NotNull HttpServletRequest request) {
        return super.isAvailable(request) && checkHasGlobalPermission(request, Permission.CHANGE_SERVER_SETTINGS);
    }

    @Override
    public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) {
        model.put("itemsToRemove", customDataStorageBuildScanCleaner.itemsToRemove());
    }

}
