package nu.studer.teamcity.buildscan.agent

class TcPluginConfig {

    URI gradlePluginRepositoryUrl
    URI geUrl
    boolean geAllowUntrustedServer
    String gePluginVersion
    String ccudPluginVersion
    String geExtensionVersion
    String ccudExtensionVersion
    boolean enableCommandLineRunner

    // configuration params as they would be set by the user in the TeamCity configuration
    Map<String, String> toConfigParameters() {
        Map<String, String> configProps = [:]
        if (gradlePluginRepositoryUrl) {
            configProps.put 'buildScanPlugin.gradle.plugin-repository.url', gradlePluginRepositoryUrl.toString()
        }
        if (geUrl) {
            configProps.put 'buildScanPlugin.gradle-enterprise.url', geUrl.toString()
        }
        if (geAllowUntrustedServer) {
            configProps.put 'buildScanPlugin.gradle-enterprise.allow-untrusted-server', 'true'
        }
        if (gePluginVersion) {
            configProps.put 'buildScanPlugin.gradle-enterprise.plugin.version', gePluginVersion
        }
        if (ccudPluginVersion) {
            configProps.put 'buildScanPlugin.ccud.plugin.version', ccudPluginVersion
        }
        if (geExtensionVersion) {
            configProps.put 'buildScanPlugin.gradle-enterprise.extension.version', geExtensionVersion
        }
        if (ccudExtensionVersion) {
            configProps.put 'buildScanPlugin.ccud.extension.version', ccudExtensionVersion
        }
        if (enableCommandLineRunner) {
            configProps.put 'buildScanPlugin.command-line-build-step.enabled', 'true'
        }
        configProps
    }
}
