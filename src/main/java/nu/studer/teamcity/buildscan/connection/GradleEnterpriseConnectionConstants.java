package nu.studer.teamcity.buildscan.connection;

@SuppressWarnings("unused")
public final class GradleEnterpriseConnectionConstants {

    // Constants defined by the Gradle Enterprise Connection
    // These are used to correlate data set by the user in the connection dialog to the descriptor parameters available in the Project Feature Descriptor Parameters

    public static final String GRADLE_ENTERPRISE_URL = "gradleEnterpriseUrl";
    public static final String GRADLE_ENTERPRISE_ACCESS_KEY = "gradleEnterpriseAccessKey";
    public static final String GRADLE_ENTERPRISE_PLUGIN_VERSION = "gradleEnterprisePluginVersion";
    public static final String COMMON_CUSTOM_USER_DATA_PLUGIN_VERSION = "commonCustomUserDataPluginVersion";
    public static final String GRADLE_ENTERPRISE_EXTENSION_VERSION = "gradleEnterpriseExtensionVersion";
    public static final String COMMON_CUSTOM_USER_DATA_EXTENSION_VERSION = "commonCustomUserDataExtensionVersion";
    public static final String ALLOW_UNTRUSTED_SERVER = "allowUntrustedServer";
    public static final String GRADLE_PLUGIN_REPOSITORY_URL = "gradlePluginRepositoryUrl";
    public static final String INSTRUMENT_COMMAND_LINE_BUILD_STEP = "instrumentCommandLineBuildStep";

    // Constants defined by the BuildScanServiceMessageInjector
    // This connection sets these values as build parameters so that they can be picked up by the BuildScanServiceMessageInjector

    public static final String GRADLE_ENTERPRISE_ACCESS_KEY_ENV_VAR = "env.GRADLE_ENTERPRISE_ACCESS_KEY";

    public static final String GRADLE_ENTERPRISE_URL_CONFIG_PARAM = "buildScanPlugin.gradle-enterprise.url";
    public static final String GRADLE_ENTERPRISE_PLUGIN_VERSION_CONFIG_PARAM = "buildScanPlugin.gradle-enterprise.plugin.version";
    public static final String COMMON_CUSTOM_USER_DATA_PLUGIN_VERSION_CONFIG_PARAM = "buildScanPlugin.ccud.plugin.version";
    public static final String GRADLE_ENTERPRISE_EXTENSION_VERSION_CONFIG_PARAM = "buildScanPlugin.gradle-enterprise.extension.version";
    public static final String COMMON_CUSTOM_USER_DATA_EXTENSION_VERSION_CONFIG_PARAM = "buildScanPlugin.ccud.extension.version";
    public static final String ALLOW_UNTRUSTED_SERVER_CONFIG_PARAM = "buildScanPlugin.gradle-enterprise.allow-untrusted-server";
    public static final String GRADLE_PLUGIN_REPOSITORY_URL_CONFIG_PARAM = "buildScanPlugin.gradle.plugin-repository.url";
    public static final String INSTRUMENT_COMMAND_LINE_BUILD_STEP_CONFIG_PARAM = "buildScanPlugin.command-line-build-step.enabled";

    public static final String GRADLE_ENTERPRISE_CONNECTION_PROVIDER = "gradle-enterprise-connection-provider";

    // These getters exist so that geConnectionDialog.jsp can read these constants as a JavaBean

    public String getGradleEnterpriseUrl() {
        return GRADLE_ENTERPRISE_URL;
    }

    public String getGradleEnterpriseAccessKey() {
        return GRADLE_ENTERPRISE_ACCESS_KEY;
    }

    public String getGradleEnterprisePluginVersion() {
        return GRADLE_ENTERPRISE_PLUGIN_VERSION;
    }

    public String getCommonCustomUserDataPluginVersion() {
        return COMMON_CUSTOM_USER_DATA_PLUGIN_VERSION;
    }

    public String getGradleEnterpriseExtensionVersion() {
        return GRADLE_ENTERPRISE_EXTENSION_VERSION;
    }

    public String getCommonCustomUserDataExtensionVersion() {
        return COMMON_CUSTOM_USER_DATA_EXTENSION_VERSION;
    }

    public String getAllowUntrustedServer() {
        return ALLOW_UNTRUSTED_SERVER;
    }

    public String getGradlePluginRepositoryUrl() {
        return GRADLE_PLUGIN_REPOSITORY_URL;
    }

    public String getInstrumentCommandLineBuildStep() {
        return INSTRUMENT_COMMAND_LINE_BUILD_STEP;
    }

}
