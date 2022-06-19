package nu.studer.teamcity.buildscan.agent.gradle

class TcPluginConfig {

    URI gradlePluginRepositoryUrl
    URI geUrl
    boolean geAllowUntrustedServer
    String gePluginVersion
    String ccudPluginVersion
    boolean enableCommandLineRunner

    Map<String, String> toConfigProperties() {
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
        if (enableCommandLineRunner) {
            configProps.put 'buildScanPlugin.command-line-build-step.enabled', 'true'
        }
        configProps
    }

    // Generate sysProps for the init-script directly, for Gradle versions where TestKit doesn't support env vars
    List<String> toSysProps() {
        def jvmArgs = ["-DteamCityBuildScanPlugin.init-script-name=build-scan-init.gradle"]
        if (gradlePluginRepositoryUrl) {
            jvmArgs << "-DteamCityBuildScanPlugin.gradle.plugin-repository.url=$gradlePluginRepositoryUrl".toString()
        }
        if (geUrl) {
            jvmArgs << "-DteamCityBuildScanPlugin.gradle-enterprise.url=$geUrl".toString()
        }
        if (geAllowUntrustedServer) {
            jvmArgs << "-DteamCityBuildScanPlugin.gradle-enterprise.allow-untrusted-server=true"
        }
        if (gePluginVersion) {
            jvmArgs << "-DteamCityBuildScanPlugin.gradle-enterprise.plugin.version=$gePluginVersion".toString()
        }
        if (ccudPluginVersion) {
            jvmArgs << "-DteamCityBuildScanPlugin.ccud.plugin.version=$ccudPluginVersion".toString()
        }
        jvmArgs
    }

}
