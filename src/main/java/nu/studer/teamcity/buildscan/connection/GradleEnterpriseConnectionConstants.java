package nu.studer.teamcity.buildscan.connection;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public final class GradleEnterpriseConnectionConstants {

    // Constants defined by the Gradle Enterprise Connection
    // These are used to correlate data set by the user in the connection dialog to the descriptor parameters available in the Project Feature Descriptor Parameters

    public static final String GRADLE_PLUGIN_REPOSITORY_URL = "gradlePluginRepositoryUrl";
    public static final String GRADLE_ENTERPRISE_URL = "gradleEnterpriseUrl";
    public static final String ALLOW_UNTRUSTED_SERVER = "allowUntrustedServer";
    public static final String GE_PLUGIN_VERSION = "gradleEnterprisePluginVersion";
    public static final String CCUD_PLUGIN_VERSION = "commonCustomUserDataPluginVersion";
    public static final String GE_EXTENSION_VERSION = "gradleEnterpriseExtensionVersion";
    public static final String CCUD_EXTENSION_VERSION = "commonCustomUserDataExtensionVersion";
    public static final String CUSTOM_GE_EXTENSION_COORDINATES = "customGradleEnterpriseExtensionCoordinates";
    public static final String CUSTOM_CCUD_EXTENSION_COORDINATES = "customCommonCustomUserDataExtensionCoordinates";
    public static final String INSTRUMENT_COMMAND_LINE_BUILD_STEP = "instrumentCommandLineBuildStep";
    public static final String GRADLE_ENTERPRISE_ACCESS_KEY = "gradleEnterpriseAccessKey";
    public static final String ENFORCE_GRADLE_ENTERPRISE_URL = "enforceGradleEnterpriseUrl";

    // Constants defined by the BuildScanServiceMessageInjector
    // This connection sets these values as build parameters so that they can be picked up by the BuildScanServiceMessageInjector

    public static final String GRADLE_PLUGIN_REPOSITORY_URL_CONFIG_PARAM = "buildScanPlugin.gradle.plugin-repository.url";
    public static final String GRADLE_ENTERPRISE_URL_CONFIG_PARAM = "buildScanPlugin.gradle-enterprise.url";
    public static final String ALLOW_UNTRUSTED_SERVER_CONFIG_PARAM = "buildScanPlugin.gradle-enterprise.allow-untrusted-server";
    public static final String GE_PLUGIN_VERSION_CONFIG_PARAM = "buildScanPlugin.gradle-enterprise.plugin.version";
    public static final String CCUD_PLUGIN_VERSION_CONFIG_PARAM = "buildScanPlugin.ccud.plugin.version";
    public static final String GE_EXTENSION_VERSION_CONFIG_PARAM = "buildScanPlugin.gradle-enterprise.extension.version";
    public static final String CCUD_EXTENSION_VERSION_CONFIG_PARAM = "buildScanPlugin.ccud.extension.version";
    public static final String CUSTOM_GE_EXTENSION_COORDINATES_CONFIG_PARAM = "buildScanPlugin.gradle-enterprise.extension.custom.coordinates";
    public static final String CUSTOM_CCUD_EXTENSION_COORDINATES_CONFIG_PARAM = "buildScanPlugin.ccud.extension.custom.coordinates";
    public static final String INSTRUMENT_COMMAND_LINE_BUILD_STEP_CONFIG_PARAM = "buildScanPlugin.command-line-build-step.enabled";
    public static final String GRADLE_ENTERPRISE_ACCESS_KEY_ENV_VAR = "env.GRADLE_ENTERPRISE_ACCESS_KEY";
    public static final String ENFORCE_GRADLE_ENTERPRISE_URL_CONFIG_PARAM = "buildScanPlugin.gradle-enterprise.enforce-url";

    // Bundled version constants
    // These are used to define the versions of the Gradle Enterprise and Common Custom User Data Extensions that are bundle with this plugin

    public static final List<String> BUNDLED_GE_EXTENSION_VERSIONS = Arrays.asList("1.17.2", "1.16.6", "1.15.5");
    public static final List<String> BUNDLED_CCUD_EXTENSION_VERSIONS = Collections.singletonList("1.11.1");

    public static final String GRADLE_ENTERPRISE_CONNECTION_PROVIDER = "gradle-enterprise-connection-provider";

    // The below getters exist so that geConnectionDialog.jsp can read these constants using JavaBean conventions

    public String getGradlePluginRepositoryUrl() {
        return GRADLE_PLUGIN_REPOSITORY_URL;
    }

    public String getGradleEnterpriseUrl() {
        return GRADLE_ENTERPRISE_URL;
    }

    public String getAllowUntrustedServer() {
        return ALLOW_UNTRUSTED_SERVER;
    }

    public String getGradleEnterprisePluginVersion() {
        return GE_PLUGIN_VERSION;
    }

    public String getCommonCustomUserDataPluginVersion() {
        return CCUD_PLUGIN_VERSION;
    }

    public String getGradleEnterpriseExtensionVersion() {
        return GE_EXTENSION_VERSION;
    }

    public String getCommonCustomUserDataExtensionVersion() {
        return CCUD_EXTENSION_VERSION;
    }

    public String getCustomGradleEnterpriseExtensionCoordinates() {
        return CUSTOM_GE_EXTENSION_COORDINATES;
    }

    public String getCustomCommonCustomUserDataExtensionCoordinates() {
        return CUSTOM_CCUD_EXTENSION_COORDINATES;
    }

    public String getInstrumentCommandLineBuildStep() {
        return INSTRUMENT_COMMAND_LINE_BUILD_STEP;
    }

    public String getGradleEnterpriseAccessKey() {
        return GRADLE_ENTERPRISE_ACCESS_KEY;
    }

    public String getEnforceGradleEnterpriseUrl() {
        return ENFORCE_GRADLE_ENTERPRISE_URL;
    }

    public List<String> getGeExtensionVersions() {
        return BUNDLED_GE_EXTENSION_VERSIONS;
    }

    public List<String> getCcudExtensionVersions() {
        return BUNDLED_CCUD_EXTENSION_VERSIONS;
    }
}
