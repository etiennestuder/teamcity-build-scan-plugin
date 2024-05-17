package nu.studer.teamcity.buildscan.connection;

@SuppressWarnings("unused")
public final class DevelocityConnectionConstants {

    // Constants defined by the Develocity Connection
    // These are used to correlate data set by the user in the connection dialog to the descriptor parameters available in the Project Feature Descriptor Parameters

    public static final String GRADLE_PLUGIN_REPOSITORY_URL = "gradlePluginRepositoryUrl";
    public static final String DEVELOCITY_URL = "gradleEnterpriseUrl";
    public static final String ALLOW_UNTRUSTED_SERVER = "allowUntrustedServzer";
    public static final String DEVELOCITY_PLUGIN_VERSION = "gradleEnterprisePluginVersion";
    public static final String CCUD_PLUGIN_VERSION = "commonCustomUserDataPluginVersion";
    public static final String DEVELOCITY_EXTENSION_VERSION = "gradleEnterpriseExtensionVersion";
    public static final String CCUD_EXTENSION_VERSION = "commonCustomUserDataExtensionVersion";
    public static final String CUSTOM_DEVELOCITY_EXTENSION_COORDINATES = "customGradleEnterpriseExtensionCoordinates";
    public static final String CUSTOM_CCUD_EXTENSION_COORDINATES = "customCommonCustomUserDataExtensionCoordinates";
    public static final String INSTRUMENT_COMMAND_LINE_BUILD_STEP = "instrumentCommandLineBuildStep";
    public static final String GRADLE_ENTERPRISE_ACCESS_KEY = "gradleEnterpriseAccessKey";
    public static final String ENFORCE_DEVELOCITY_URL = "enforceGradleEnterpriseUrl";

    // Constants defined by the BuildScanServiceMessageInjector
    // This connection sets these values as build parameters so that they can be picked up by the BuildScanServiceMessageInjector

    public static final String GRADLE_PLUGIN_REPOSITORY_URL_CONFIG_PARAM = "buildScanPlugin.gradle.plugin-repository.url";
    public static final String DEVELOCITY_URL_CONFIG_PARAM = "buildScanPlugin.gradle-enterprise.url";
    public static final String ALLOW_UNTRUSTED_SERVER_CONFIG_PARAM = "buildScanPlugin.gradle-enterprise.allow-untrusted-server";
    public static final String DEVELOCITY_PLUGIN_VERSION_CONFIG_PARAM = "buildScanPlugin.gradle-enterprise.plugin.version";
    public static final String CCUD_PLUGIN_VERSION_CONFIG_PARAM = "buildScanPlugin.ccud.plugin.version";
    public static final String DEVELOCITY_EXTENSION_VERSION_CONFIG_PARAM = "buildScanPlugin.gradle-enterprise.extension.version";
    public static final String CCUD_EXTENSION_VERSION_CONFIG_PARAM = "buildScanPlugin.ccud.extension.version";
    public static final String CUSTOM_DEVELOCITY_EXTENSION_COORDINATES_CONFIG_PARAM = "buildScanPlugin.gradle-enterprise.extension.custom.coordinates";
    public static final String CUSTOM_CCUD_EXTENSION_COORDINATES_CONFIG_PARAM = "buildScanPlugin.ccud.extension.custom.coordinates";
    public static final String INSTRUMENT_COMMAND_LINE_BUILD_STEP_CONFIG_PARAM = "buildScanPlugin.command-line-build-step.enabled";
    public static final String GRADLE_ENTERPRISE_ACCESS_KEY_ENV_VAR = "env.GRADLE_ENTERPRISE_ACCESS_KEY";
    public static final String ENFORCE_DEVELOCITY_URL_CONFIG_PARAM = "buildScanPlugin.gradle-enterprise.enforce-url";

    public static final String DEVELOCITY_CONNECTION_PROVIDER = "gradle-enterprise-connection-provider";

    // The below getters exist so that develocityConnectionDialog.jsp can read these constants using JavaBean conventions

    public String getGradlePluginRepositoryUrl() {
        return GRADLE_PLUGIN_REPOSITORY_URL;
    }

    public String getDevelocityUrl() {
        return DEVELOCITY_URL;
    }

    public String getAllowUntrustedServer() {
        return ALLOW_UNTRUSTED_SERVER;
    }

    public String getDevelocityPluginVersion() {
        return DEVELOCITY_PLUGIN_VERSION;
    }

    public String getCommonCustomUserDataPluginVersion() {
        return CCUD_PLUGIN_VERSION;
    }

    public String getDevelocityExtensionVersion() {
        return DEVELOCITY_EXTENSION_VERSION;
    }

    public String getCommonCustomUserDataExtensionVersion() {
        return CCUD_EXTENSION_VERSION;
    }

    public String getCustomDevelocityExtensionCoordinates() {
        return CUSTOM_DEVELOCITY_EXTENSION_COORDINATES;
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

    public String getEnforceDevelocityUrl() {
        return ENFORCE_DEVELOCITY_URL;
    }

}
