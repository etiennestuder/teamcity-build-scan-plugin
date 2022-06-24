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

/**
 * This implementation of {@link BuildParametersProvider} injects configuration parameters and environment variables
 * needed in order to automatically apply Gradle Enterprise to Gradle and Maven builds, based on the configuration of
 * the connection.
 */
@SuppressWarnings({"DuplicatedCode", "Convert2Diamond"})
public final class GradleEnterpriseParametersProvider implements BuildParametersProvider {
    private static final String OVERRIDE_STRING = "UNDEFINED";

    @NotNull
    @Override
    public Map<String, String> getParameters(@NotNull SBuild build, boolean emulationMode) {
        List<Map<String, String>> allConnectionParameters = getAllConnectionParameters(build);

        // descriptorParameters can contain null values, but TeamCity handles these null parameters as if they were not set
        Map<String, String> params = new HashMap<>();

        for(int i = allConnectionParameters.size() - 1; i >= 0; i--) {
            Map<String, String> connectionParameters = allConnectionParameters.get(i);

            setParameter(params, GRADLE_PLUGIN_REPOSITORY_URL_CONFIG_PARAM, connectionParameters.get(GRADLE_PLUGIN_REPOSITORY_URL));
            setParameter(params, GRADLE_ENTERPRISE_URL_CONFIG_PARAM, connectionParameters.get(GRADLE_ENTERPRISE_URL));
            setParameter(params, ALLOW_UNTRUSTED_SERVER_CONFIG_PARAM, connectionParameters.get(ALLOW_UNTRUSTED_SERVER));
            setParameter(params, GE_PLUGIN_VERSION_CONFIG_PARAM, connectionParameters.get(GE_PLUGIN_VERSION));
            setParameter(params, CCUD_PLUGIN_VERSION_CONFIG_PARAM, connectionParameters.get(CCUD_PLUGIN_VERSION));
            setParameter(params, GE_EXTENSION_VERSION_CONFIG_PARAM, connectionParameters.get(GE_EXTENSION_VERSION));
            setParameter(params, CCUD_EXTENSION_VERSION_CONFIG_PARAM, connectionParameters.get(CCUD_EXTENSION_VERSION));
            setParameter(params, CUSTOM_GE_EXTENSION_COORDINATES_CONFIG_PARAM, connectionParameters.get(CUSTOM_GE_EXTENSION_COORDINATES));
            setParameter(params, CUSTOM_CCUD_EXTENSION_COORDINATES_CONFIG_PARAM, connectionParameters.get(CUSTOM_CCUD_EXTENSION_COORDINATES));
            setParameter(params, INSTRUMENT_COMMAND_LINE_BUILD_STEP_CONFIG_PARAM, connectionParameters.get(INSTRUMENT_COMMAND_LINE_BUILD_STEP));
            setParameter(params, GRADLE_ENTERPRISE_ACCESS_KEY_ENV_VAR, connectionParameters.get(GRADLE_ENTERPRISE_ACCESS_KEY));
        }
        return params;
    }

    private static void setParameter(Map<String, String> params, String key, String value) {
        if (params.containsKey(key) && OVERRIDE_STRING.equals(value)) {
            params.remove(key);
        } else if (value != null) {
            params.put(key, value);
        }
    }

    @NotNull
    private List<Map<String, String>> getAllConnectionParameters(@NotNull SBuild build) {
        List<Map<String, String>> descriptors = new ArrayList<Map<String, String>>();

        SBuildType buildType = build.getBuildType();
        if (buildType == null) {
            return descriptors;
        }

        Collection<SProjectFeatureDescriptor> connections = buildType.getProject().getAvailableFeaturesOfType(OAuthConstants.FEATURE_TYPE);
        for (SProjectFeatureDescriptor descriptor : connections) {
            Map<String, String> parameters = descriptor.getParameters();
            String oauthProviderType = parameters.get(OAuthConstants.OAUTH_TYPE_PARAM);
            if (GRADLE_ENTERPRISE_CONNECTION_PROVIDER.equals(oauthProviderType)) {
                descriptors.add(parameters);
            }
        }

        return descriptors;
    }

    @NotNull
    @Override
    public Collection<String> getParametersAvailableOnAgent(@NotNull SBuild build) {
        return Collections.emptyList();
    }

}
