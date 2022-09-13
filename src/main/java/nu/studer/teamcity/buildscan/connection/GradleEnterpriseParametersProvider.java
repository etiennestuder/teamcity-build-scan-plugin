package nu.studer.teamcity.buildscan.connection;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor;
import jetbrains.buildServer.serverSide.oauth.OAuthConstants;
import jetbrains.buildServer.serverSide.parameters.BuildParametersProvider;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.ALLOW_UNTRUSTED_SERVER;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.ALLOW_UNTRUSTED_SERVER_CONFIG_PARAM;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.CCUD_EXTENSION_VERSION;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.CCUD_EXTENSION_VERSION_CONFIG_PARAM;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.CCUD_PLUGIN_VERSION;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.CCUD_PLUGIN_VERSION_CONFIG_PARAM;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.CUSTOM_CCUD_EXTENSION_COORDINATES;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.CUSTOM_CCUD_EXTENSION_COORDINATES_CONFIG_PARAM;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.CUSTOM_GE_EXTENSION_COORDINATES;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.CUSTOM_GE_EXTENSION_COORDINATES_CONFIG_PARAM;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GE_EXTENSION_VERSION;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GE_EXTENSION_VERSION_CONFIG_PARAM;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GE_PLUGIN_VERSION;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GE_PLUGIN_VERSION_CONFIG_PARAM;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GRADLE_ENTERPRISE_ACCESS_KEY;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GRADLE_ENTERPRISE_ACCESS_KEY_ENV_VAR;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GRADLE_ENTERPRISE_CONNECTION_PROVIDER;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GRADLE_ENTERPRISE_URL;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GRADLE_ENTERPRISE_URL_CONFIG_PARAM;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GRADLE_PLUGIN_REPOSITORY_URL;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GRADLE_PLUGIN_REPOSITORY_URL_CONFIG_PARAM;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.INSTRUMENT_COMMAND_LINE_BUILD_STEP;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.INSTRUMENT_COMMAND_LINE_BUILD_STEP_CONFIG_PARAM;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.IS_MAVEN_VERSION_CHECK_ENABLED;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.IS_MAVEN_VERSION_CHECK_ENABLED_PARAM;

/**
 * This implementation of {@link BuildParametersProvider} injects configuration parameters and environment variables
 * needed in order to automatically apply Gradle Enterprise to Gradle and Maven builds, based on the configuration of
 * the connection.
 */
@SuppressWarnings({"DuplicatedCode", "Convert2Diamond"})
public final class GradleEnterpriseParametersProvider implements BuildParametersProvider {

    @NotNull
    @Override
    public Map<String, String> getParameters(@NotNull SBuild build, boolean emulationMode) {
        List<Map<String, String>> connections = getAllGeConnections(build);

        // descriptorParameters can contain null values, but TeamCity handles these null parameters as if they were not set
        Map<String, String> params = new HashMap<>();
        for (int i = connections.size() - 1; i >= 0; i--) {
            Map<String, String> connectionParams = connections.get(i);
            setParameter(GRADLE_PLUGIN_REPOSITORY_URL_CONFIG_PARAM, connectionParams.get(GRADLE_PLUGIN_REPOSITORY_URL), params);
            setParameter(GRADLE_ENTERPRISE_URL_CONFIG_PARAM, connectionParams.get(GRADLE_ENTERPRISE_URL), params);
            setParameter(ALLOW_UNTRUSTED_SERVER_CONFIG_PARAM, connectionParams.get(ALLOW_UNTRUSTED_SERVER), params);
            setParameter(GE_PLUGIN_VERSION_CONFIG_PARAM, connectionParams.get(GE_PLUGIN_VERSION), params);
            setParameter(CCUD_PLUGIN_VERSION_CONFIG_PARAM, connectionParams.get(CCUD_PLUGIN_VERSION), params);
            setParameter(GE_EXTENSION_VERSION_CONFIG_PARAM, connectionParams.get(GE_EXTENSION_VERSION), params);
            setParameter(CCUD_EXTENSION_VERSION_CONFIG_PARAM, connectionParams.get(CCUD_EXTENSION_VERSION), params);
            setParameter(CUSTOM_GE_EXTENSION_COORDINATES_CONFIG_PARAM, connectionParams.get(CUSTOM_GE_EXTENSION_COORDINATES), params);
            setParameter(CUSTOM_CCUD_EXTENSION_COORDINATES_CONFIG_PARAM, connectionParams.get(CUSTOM_CCUD_EXTENSION_COORDINATES), params);
            setParameter(INSTRUMENT_COMMAND_LINE_BUILD_STEP_CONFIG_PARAM, connectionParams.get(INSTRUMENT_COMMAND_LINE_BUILD_STEP), params);
            setParameter(IS_MAVEN_VERSION_CHECK_ENABLED_PARAM, connectionParams.get(IS_MAVEN_VERSION_CHECK_ENABLED), params);
            setParameter(GRADLE_ENTERPRISE_ACCESS_KEY_ENV_VAR, connectionParams.get(GRADLE_ENTERPRISE_ACCESS_KEY), params);
        }
        return params;
    }

    private static void setParameter(String key, String value, Map<String, String> params) {
        if (value != null) {
            params.put(key, value);
        }
    }

    @NotNull
    private static List<Map<String, String>> getAllGeConnections(@NotNull SBuild build) {
        SBuildType buildType = build.getBuildType();
        if (buildType == null) {
            return Collections.emptyList();
        }

        List<Map<String, String>> connections = new ArrayList<Map<String, String>>();
        Collection<SProjectFeatureDescriptor> descriptors = buildType.getProject().getAvailableFeaturesOfType(OAuthConstants.FEATURE_TYPE);
        for (SProjectFeatureDescriptor descriptor : descriptors) {
            Map<String, String> parameters = descriptor.getParameters();
            String connectionType = parameters.get(OAuthConstants.OAUTH_TYPE_PARAM);
            if (GRADLE_ENTERPRISE_CONNECTION_PROVIDER.equals(connectionType)) {
                connections.add(parameters);
            }
        }
        return connections;
    }

    @NotNull
    @Override
    public Collection<String> getParametersAvailableOnAgent(@NotNull SBuild build) {
        return Collections.emptyList();
    }

}
