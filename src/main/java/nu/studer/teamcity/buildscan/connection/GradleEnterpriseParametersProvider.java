package nu.studer.teamcity.buildscan.connection;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor;
import jetbrains.buildServer.serverSide.oauth.OAuthConstants;
import jetbrains.buildServer.serverSide.parameters.BuildParametersProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.ALLOW_UNTRUSTED_SERVER;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.ALLOW_UNTRUSTED_SERVER_CONFIG_PARAM;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.CCUD_EXTENSION_VERSION;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.CCUD_EXTENSION_VERSION_CONFIG_PARAM;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.CCUD_PLUGIN_VERSION;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.CCUD_PLUGIN_VERSION_CONFIG_PARAM;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GRADLE_ENTERPRISE_ACCESS_KEY;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GRADLE_ENTERPRISE_ACCESS_KEY_ENV_VAR;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GRADLE_ENTERPRISE_CONNECTION_PROVIDER;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GE_EXTENSION_VERSION;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GE_EXTENSION_VERSION_CONFIG_PARAM;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GE_PLUGIN_VERSION;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GE_PLUGIN_VERSION_CONFIG_PARAM;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GRADLE_ENTERPRISE_URL;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GRADLE_ENTERPRISE_URL_CONFIG_PARAM;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GRADLE_PLUGIN_REPOSITORY_URL;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GRADLE_PLUGIN_REPOSITORY_URL_CONFIG_PARAM;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.INSTRUMENT_COMMAND_LINE_BUILD_STEP;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.INSTRUMENT_COMMAND_LINE_BUILD_STEP_CONFIG_PARAM;

/**
 * This implementation of {@link BuildParametersProvider} injects configuration parameters and environment variables
 * needed in order to automatically apply Gradle Enterprise to Gradle and Maven builds, based on the configuration of
 * the connection.
 */
public final class GradleEnterpriseParametersProvider implements BuildParametersProvider {

    @NotNull
    @Override
    public Map<String, String> getParameters(@NotNull SBuild build, boolean emulationMode) {
        SProjectFeatureDescriptor descriptor = getProjectFeatureDescriptor(build);
        if (descriptor == null) {
            return Collections.emptyMap();
        }

        Map<String, String> descriptorParameters = descriptor.getParameters();

        // descriptorParameters can contain null values, but TeamCity handles these null parameters as if they were not set
        Map<String, String> params = new HashMap<>();
        params.put(GRADLE_PLUGIN_REPOSITORY_URL_CONFIG_PARAM, descriptorParameters.get(GRADLE_PLUGIN_REPOSITORY_URL));
        params.put(GRADLE_ENTERPRISE_URL_CONFIG_PARAM, descriptorParameters.get(GRADLE_ENTERPRISE_URL));
        params.put(ALLOW_UNTRUSTED_SERVER_CONFIG_PARAM, descriptorParameters.get(ALLOW_UNTRUSTED_SERVER));
        params.put(GE_PLUGIN_VERSION_CONFIG_PARAM, descriptorParameters.get(GE_PLUGIN_VERSION));
        params.put(CCUD_PLUGIN_VERSION_CONFIG_PARAM, descriptorParameters.get(CCUD_PLUGIN_VERSION));
        params.put(GE_EXTENSION_VERSION_CONFIG_PARAM, descriptorParameters.get(GE_EXTENSION_VERSION));
        params.put(CCUD_EXTENSION_VERSION_CONFIG_PARAM, descriptorParameters.get(CCUD_EXTENSION_VERSION));
        params.put(INSTRUMENT_COMMAND_LINE_BUILD_STEP_CONFIG_PARAM, descriptorParameters.get(INSTRUMENT_COMMAND_LINE_BUILD_STEP));
        params.put(GRADLE_ENTERPRISE_ACCESS_KEY_ENV_VAR, descriptorParameters.get(GRADLE_ENTERPRISE_ACCESS_KEY));
        return params;
    }

    @Nullable
    private SProjectFeatureDescriptor getProjectFeatureDescriptor(@NotNull SBuild build) {
        SBuildType buildType = build.getBuildType();
        if (buildType == null) {
            return null;
        }

        // Find the first connection that matches the GE provider type. From testing, this seems to have the following ordering behavior:
        // - If there are duplicate connections on a project, the newest one wins
        // - Connections on subprojects can override connections on the root project regardless of age
        Collection<SProjectFeatureDescriptor> connections = buildType.getProject().getAvailableFeaturesOfType(OAuthConstants.FEATURE_TYPE);
        for (SProjectFeatureDescriptor descriptor : connections) {
            String oauthProviderType = descriptor.getParameters().get(OAuthConstants.OAUTH_TYPE_PARAM);
            if (GRADLE_ENTERPRISE_CONNECTION_PROVIDER.equals(oauthProviderType)) {
                return descriptor;
            }
        }

        return null;
    }

    @NotNull
    @Override
    public Collection<String> getParametersAvailableOnAgent(@NotNull SBuild build) {
        return Collections.emptyList();
    }

}
