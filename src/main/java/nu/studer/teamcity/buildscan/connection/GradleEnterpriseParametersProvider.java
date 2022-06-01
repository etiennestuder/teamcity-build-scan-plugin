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

import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.*;

/**
 * This implementation of {@link BuildParametersProvider} injects configuration parameters and environment variables
 * needed in order to automatically apply Gradle Enterprise to Gradle and Maven builds, based on configuration of the
 * connection.
 */
public final class GradleEnterpriseParametersProvider implements BuildParametersProvider {

    @NotNull
    @Override
    public Map<String, String> getParameters(@NotNull SBuild build, boolean emulationMode) {
        SProjectFeatureDescriptor descriptor = getsProjectFeatureDescriptor(build);

        Map<String, String> params = new HashMap<>();
        if (descriptor != null) {
            Map<String, String> descriptorParameters = descriptor.getParameters();

            params.put(GRADLE_ENTERPRISE_URL_CONFIG_PARAM, descriptorParameters.get(GRADLE_ENTERPRISE_URL));
            params.put(GRADLE_ENTERPRISE_PLUGIN_VERSION_CONFIG_PARAM, descriptorParameters.get(GRADLE_ENTERPRISE_PLUGIN_VERSION));
            params.put(COMMON_CUSTOM_USER_DATA_PLUGIN_VERSION_CONFIG_PARAM, descriptorParameters.get(COMMON_CUSTOM_USER_DATA_PLUGIN_VERSION));
            params.put(GRADLE_ENTERPRISE_EXTENSION_VERSION_CONFIG_PARAM, descriptorParameters.get(GRADLE_ENTERPRISE_EXTENSION_VERSION));
            params.put(COMMON_CUSTOM_USER_DATA_EXTENSION_VERSION_CONFIG_PARAM, descriptorParameters.get(COMMON_CUSTOM_USER_DATA_EXTENSION_VERSION));
            params.put(ALLOW_UNTRUSTED_SERVER_CONFIG_PARAM, descriptorParameters.get(ALLOW_UNTRUSTED_SERVER));

            params.put(GRADLE_ENTERPRISE_ACCESS_KEY_ENV_VAR, descriptorParameters.get(GRADLE_ENTERPRISE_ACCESS_KEY));
        }
        return params;
    }

    @Nullable
    private SProjectFeatureDescriptor getsProjectFeatureDescriptor(@NotNull SBuild build) {
        SBuildType buildType = build.getBuildType();
        if (buildType != null) {
            Collection<SProjectFeatureDescriptor> oauthConnections = buildType.getProject().getAvailableFeaturesOfType(OAuthConstants.FEATURE_TYPE);

            // This logic will find the first descriptor that matches the GE provider type. From testing, this seems to have the following behavior:
            // - If there are duplicate connections on a project, the newest one wins
            // - Connections on sub-projects can override connections on the root project regardless of age
            for (SProjectFeatureDescriptor descriptor : oauthConnections) {
                String oauthProviderType = descriptor.getParameters().get(OAuthConstants.OAUTH_TYPE_PARAM);
                if (GRADLE_ENTERPRISE_CONNECTION_PROVIDER.equals(oauthProviderType)) {
                    return descriptor;
                }
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
