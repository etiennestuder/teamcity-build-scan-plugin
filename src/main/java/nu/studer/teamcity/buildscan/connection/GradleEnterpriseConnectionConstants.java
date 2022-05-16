package nu.studer.teamcity.buildscan.connection;

public final class GradleEnterpriseConnectionConstants {
    public static final String GRADLE_ENTERPRISE_URL = "gradleEnterpriseUrl";
    public static final String GRADLE_ENTERPRISE_ACCESS_KEY = "gradleEnterpriseAccessKey";
    public static final String GRADLE_ENTERPRISE_PLUGIN_VERSION = "gradleEnterprisePluginVersion";
    public static final String COMMON_CUSTOM_USER_DATA_PLUGIN_VERSION = "commonCustomUserDataPluginVersion";
    public static final String GRADLE_ENTERPRISE_EXTENSION_VERSION = "gradleEnterpriseExtensionVersion";
    public static final String COMMON_CUSTOM_USER_DATA_EXTENSION_VERSION = "commonCustomUserDataExtensionVersion";

    public static final String GRADLE_ENTERPRISE_ACCESS_KEY_ENV_VAR = "env.GRADLE_ENTERPRISE_ACCESS_KEY";

    public static final String GRADLE_ENTERPRISE_CONNECTION_PROVIDER = "gradle-enterprise-connection-provider";

    // These getters exist so that ge-connection-dialog.jsp can read these constants as a JavaBean

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
}
