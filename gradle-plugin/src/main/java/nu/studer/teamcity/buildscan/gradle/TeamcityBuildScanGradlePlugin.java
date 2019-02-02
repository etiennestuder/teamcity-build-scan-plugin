package nu.studer.teamcity.buildscan.gradle;

import com.gradle.scan.plugin.BuildScanExtension;
import com.gradle.scan.plugin.PublishedBuildScan;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.AppliedPlugin;

import java.lang.reflect.Method;

public class TeamcityBuildScanGradlePlugin implements Plugin<Project> {

    private static final String BUILD_SCAN_PLUGIN_ID = "com.gradle.build-scan";
    private static final String BUILD_SCAN_MESSAGE_NAME = "buildscan";
    private static final String GRADLE_BUILDSCAN_TEAMCITY_PLUGIN = "GRADLE_BUILDSCAN_TEAMCITY_PLUGIN";
    private static final String TEAMCITY_VERSION = "TEAMCITY_VERSION";

    @Override
    public void apply(Project project) {
        // only register callback if this is a teamcity build, and we are not using the gradle build runner
        if (System.getenv(TEAMCITY_VERSION) != null && System.getenv(GRADLE_BUILDSCAN_TEAMCITY_PLUGIN) == null) {
            project.getPluginManager().withPlugin(BUILD_SCAN_PLUGIN_ID, new Action<AppliedPlugin>() {
                @Override
                public void execute(AppliedPlugin appliedPlugin) {
                    BuildScanExtension buildScanExtension = project.getExtensions().getByType(BuildScanExtension.class);

                    if (supportsScanPublishedListener(buildScanExtension)) {
                        buildScanExtension.buildScanPublished(new Action<PublishedBuildScan>() {
                            @Override
                            public void execute(PublishedBuildScan publishedBuildScan) {
                                project.getLogger().quiet(ServiceMessage.of(BUILD_SCAN_MESSAGE_NAME, publishedBuildScan.getBuildScanUri().toString()).toString());
                            }
                        });
                    }
                }
            });
        }
    }

    private static boolean supportsScanPublishedListener(BuildScanExtension extension) {
        Class clazz = extension.getClass();
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals("buildScanPublished"))
                return true;
        }
        return false;
    }
}
