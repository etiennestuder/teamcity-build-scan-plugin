package nu.studer.teamcity.buildscan.internal.cleanup;

import jetbrains.buildServer.controllers.BaseAjaxActionController;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import jetbrains.buildServer.web.openapi.ControllerAction;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import nu.studer.teamcity.buildscan.internal.CustomDataStorageBuildScanCleaner;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static jetbrains.buildServer.serverSide.auth.AuthUtil.hasGlobalPermission;

public class BuildScanCleanupController extends BaseAjaxActionController implements ControllerAction {
    private final CustomDataStorageBuildScanCleaner customDataStorageCleaner;
    private final SecurityContext securityContext;

    public BuildScanCleanupController(@NotNull WebControllerManager controllerManager,
                                      @NotNull CustomDataStorageBuildScanCleaner customDataStorageCleaner,
                                      @NotNull SecurityContext securityContext) {
        super(controllerManager);
        this.customDataStorageCleaner = customDataStorageCleaner;
        this.securityContext = securityContext;
        controllerManager.registerController("/admin/buildScanCleanup.html", this);
        this.registerAction(this);
    }

    @Override
    public boolean canProcess(@NotNull HttpServletRequest request) {
        return hasGlobalPermission(securityContext.getAuthorityHolder(), Permission.CONFIGURE_SERVER_DATA_CLEANUP);
    }

    @Override
    public void process(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @Nullable Element ajaxResponse) {
        customDataStorageCleaner.removeStoredItems();
    }
}
