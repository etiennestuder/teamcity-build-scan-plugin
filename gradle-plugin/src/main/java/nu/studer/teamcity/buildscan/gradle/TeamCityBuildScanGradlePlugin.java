package nu.studer.teamcity.buildscan.gradle;

import com.gradle.scan.plugin.BuildScanExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.lang.reflect.Method;

@SuppressWarnings("unused")
public class TeamCityBuildScanGradlePlugin implements Plugin<Project> {

    private static final String TEAMCITY_VERSION_ENV = "TEAMCITY_VERSION";
    private static final String GRADLE_BUILDSCAN_TEAMCITY_PLUGIN_ENV = "GRADLE_BUILDSCAN_TEAMCITY_PLUGIN";
    private static final String BUILD_SCAN_PLUGIN_ID = "com.gradle.build-scan";
    private static final String BUILD_SCAN_SERVICE_MESSAGE_NAME = "buildscan";

    @Override
    public void apply(Project project) {
        // only register callback if this is a TeamCity build, and we are _not_ using the Gradle build runner
        if (System.getenv(TEAMCITY_VERSION_ENV) != null && System.getenv(GRADLE_BUILDSCAN_TEAMCITY_PLUGIN_ENV) == null) {
            project.getPluginManager().withPlugin(BUILD_SCAN_PLUGIN_ID, appliedPlugin -> {
                BuildScanExtension buildScanExtension = project.getExtensions().getByType(BuildScanExtension.class);
                if (supportsScanPublishedListener(buildScanExtension)) {
                    buildScanExtension.buildScanPublished(publishedBuildScan -> {
                            ServiceMessage serviceMessage = ServiceMessage.of(BUILD_SCAN_SERVICE_MESSAGE_NAME, publishedBuildScan.getBuildScanUri().toString());
                            project.getLogger().quiet(serviceMessage.toString());
                        }
                    );
                }
            });
        }
    }

    private static boolean supportsScanPublishedListener(BuildScanExtension extension) {
        Class clazz = extension.getClass();
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals("buildScanPublished")) {
                return true;
            }
        }
        return false;
    }

}
