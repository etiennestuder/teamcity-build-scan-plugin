package nu.studer.teamcity.buildscan.agent

class TcPluginConfig {

    URI gradlePluginRepositoryUrl
    URI develocityUrl
    boolean develocityAllowUntrustedServer
    boolean develocityEnforceUrl
    String develocityPluginVersion
    String ccudPluginVersion
    String develocityExtensionVersion
    String ccudExtensionVersion
    String develocityExtensionCustomCoordinates
    String ccudExtensionCustomCoordinates
    boolean enableCommandLineRunner

    // configuration params as they would be set by the user in the TeamCity configuration
    Map<String, String> toConfigParameters() {
        Map<String, String> configProps = [:]
        if (gradlePluginRepositoryUrl) {
            configProps.put 'buildScanPlugin.gradle.plugin-repository.url', gradlePluginRepositoryUrl.toString()
        }
        if (develocityUrl) {
            configProps.put 'buildScanPlugin.gradle-enterprise.url', develocityUrl.toString()
        }
        if (develocityAllowUntrustedServer) {
            configProps.put 'buildScanPlugin.gradle-enterprise.allow-untrusted-server', 'true'
        }
        if (develocityEnforceUrl) {
            configProps.put 'buildScanPlugin.gradle-enterprise.enforce-url', 'true'
        }
        if (develocityPluginVersion) {
            configProps.put 'buildScanPlugin.gradle-enterprise.plugin.version', develocityPluginVersion
        }
        if (ccudPluginVersion) {
            configProps.put 'buildScanPlugin.ccud.plugin.version', ccudPluginVersion
        }
        if (develocityExtensionVersion) {
            configProps.put 'buildScanPlugin.gradle-enterprise.extension.version', develocityExtensionVersion
        }
        if (ccudExtensionVersion) {
            configProps.put 'buildScanPlugin.ccud.extension.version', ccudExtensionVersion
        }
        if (develocityExtensionCustomCoordinates) {
            configProps.put('buildScanPlugin.gradle-enterprise.extension.custom.coordinates', develocityExtensionCustomCoordinates)
        }
        if (ccudExtensionCustomCoordinates) {
            configProps.put('buildScanPlugin.ccud.extension.custom.coordinates', ccudExtensionCustomCoordinates)
        }
        if (enableCommandLineRunner) {
            configProps.put 'buildScanPlugin.command-line-build-step.enabled', 'true'
        }
        configProps
    }

}
