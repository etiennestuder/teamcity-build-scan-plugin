package nu.studer.teamcity.buildscan.connection;

import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.oauth.OAuthConnectionDescriptor;
import jetbrains.buildServer.serverSide.oauth.OAuthProvider;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.ALLOW_UNTRUSTED_SERVER;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.CCUD_EXTENSION_VERSION;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.CCUD_PLUGIN_VERSION;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.CUSTOM_CCUD_EXTENSION_COORDINATES;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.CUSTOM_GE_EXTENSION_COORDINATES;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.ENFORCE_GRADLE_ENTERPRISE_URL;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GE_EXTENSION_VERSION;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GE_PLUGIN_VERSION;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GRADLE_ENTERPRISE_ACCESS_KEY;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GRADLE_ENTERPRISE_CONNECTION_PROVIDER;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GRADLE_ENTERPRISE_URL;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GRADLE_PLUGIN_REPOSITORY_URL;
import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.INSTRUMENT_COMMAND_LINE_BUILD_STEP;

public final class GradleEnterpriseConnectionProvider extends OAuthProvider {

    private static final Logger LOGGER = Logger.getLogger("jetbrains.buildServer.BUILDSCAN");

    private static final String DEFAULT_PLUGIN_VERSIONS_RESOURCE = "default-plugin-versions.properties";

    private final PluginDescriptor descriptor;

    public GradleEnterpriseConnectionProvider(PluginDescriptor pluginDescriptor) {
        this.descriptor = pluginDescriptor;
    }

    @NotNull
    @Override
    public String getType() {
        return GRADLE_ENTERPRISE_CONNECTION_PROVIDER;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Gradle Enterprise";
    }

    @Nullable
    @Override
    public String getEditParametersUrl() {
        return descriptor.getPluginResourcesPath("geConnectionDialog.jsp");
    }

    @NotNull
    @Override
    public String describeConnection(@NotNull OAuthConnectionDescriptor connection) {
        Map<String, String> params = connection.getParameters();

        String description = "Gradle Enterprise Connection Settings:\n";

        String geUrl = params.get(GRADLE_ENTERPRISE_URL);
        if (geUrl != null) {
            description += String.format("* Gradle Enterprise Server URL: %s\n", geUrl);
        }

        String allowUntrustedServer = params.get(ALLOW_UNTRUSTED_SERVER);
        if (allowUntrustedServer != null) {
            description += String.format("* Allow Untrusted Server: %s\n", allowUntrustedServer);
        }

        String enforceGeUrl = params.get(ENFORCE_GRADLE_ENTERPRISE_URL);
        if (enforceGeUrl != null) {
            description += String.format("* Enforce Gradle Enterprise Server URL: %s\n", enforceGeUrl);
        }

        String geAccessKey = params.get(GRADLE_ENTERPRISE_ACCESS_KEY);
        if (geAccessKey != null) {
            description += String.format("* Gradle Enterprise Access Key: %s\n", "******");
        }

        description += "\nGradle Settings:\n";

        String gePluginVersion = params.get(GE_PLUGIN_VERSION);
        if (gePluginVersion != null) {
            description += String.format("* Gradle Enterprise Gradle Plugin Version: %s\n", gePluginVersion);
        }

        String ccudPluginVersion = params.get(CCUD_PLUGIN_VERSION);
        if (ccudPluginVersion != null) {
            description += String.format("* Common Custom User Data Gradle Plugin Version: %s\n", ccudPluginVersion);
        }

        String gradlePluginRepositoryUrl = params.get(GRADLE_PLUGIN_REPOSITORY_URL);
        if (gradlePluginRepositoryUrl != null) {
            description += String.format("* Gradle Plugin Repository URL: %s\n", gradlePluginRepositoryUrl);
        }

        description += "\nMaven Settings:\n";

        String geExtensionVersion = params.get(GE_EXTENSION_VERSION);
        if (geExtensionVersion != null) {
            description += String.format("* Gradle Enterprise Maven Extension Version: %s\n", geExtensionVersion);
        }

        String ccudExtensionVersion = params.get(CCUD_EXTENSION_VERSION);
        if (ccudExtensionVersion != null) {
            description += String.format("* Common Custom User Data Maven Extension Version: %s\n", ccudExtensionVersion);
        }

        String customGeExtensionCoordinates = params.get(CUSTOM_GE_EXTENSION_COORDINATES);
        if (customGeExtensionCoordinates != null) {
            description += String.format("* Gradle Enterprise Maven Extension Custom Coordinates: %s\n", customGeExtensionCoordinates);
        }

        String customCcudExtensionCoordinates = params.get(CUSTOM_CCUD_EXTENSION_COORDINATES);
        if (customCcudExtensionCoordinates != null) {
            description += String.format("* Common Custom User Data Maven Extension Custom Coordinates: %s\n", customCcudExtensionCoordinates);
        }

        description += "\nTeamCity Build Steps Settings:\n";

        String instrumentCommandLineBuildStep = params.get(INSTRUMENT_COMMAND_LINE_BUILD_STEP);
        if (instrumentCommandLineBuildStep != null) {
            description += String.format("* Instrument Command Line Build Steps: %s\n", instrumentCommandLineBuildStep);
        }

        return description;
    }

    @Nullable
    @Override
    public Map<String, String> getDefaultProperties() {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(DEFAULT_PLUGIN_VERSIONS_RESOURCE);
        if (inputStream == null) {
            return null;
        }

        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            LOGGER.warn("Unable to load default plugin versions from " + DEFAULT_PLUGIN_VERSIONS_RESOURCE, e);
            return null;
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Map<String, String> defaultProperties = new HashMap<>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            defaultProperties.put(entry.getKey().toString(), String.valueOf(entry.getValue()));
        }

        return defaultProperties;
    }

    @Nullable
    @Override
    public PropertiesProcessor getPropertiesProcessor() {
        return properties -> {
            List<InvalidProperty> errors = new ArrayList<>();
            String accessKey = properties.get(GRADLE_ENTERPRISE_ACCESS_KEY);
            if (accessKey != null && !GradleEnterpriseAccessKeyValidator.isValid(accessKey)) {
                errors.add(new InvalidProperty(GRADLE_ENTERPRISE_ACCESS_KEY, "Invalid access key"));
            }
            return errors;
        };
    }
}
