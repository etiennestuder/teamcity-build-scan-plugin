package nu.studer.teamcity.buildscan.agent.maven

final class MavenBuildStepConfiguration {
    String goals = 'clean package'
    String pathToPomFile = null

    String geUrl = null
    String geExtensionVersion = null
    String ccudExtensionVersion = null
    String geExtensionCustomCoordinates = null
    String ccudExtensionCustomCoordinates = null
    Boolean allowUntrustedServer = null
    Boolean commandLineBuildStepEnabled = null

    void applyTo(Map<String, String> configParameters, Map<String, String> runnerParameters) {
        applyRunnerParameters(runnerParameters)
        applyConfigParameters(configParameters)
    }

    private void applyRunnerParameters(Map<String, String> runnerParameters) {
        runnerParameters.put('goals', goals)

        if (pathToPomFile)
            runnerParameters.put('pomLocation', pathToPomFile)
    }

    private void applyConfigParameters(Map<String, String> configParameters) {
        if (geUrl)
            configParameters.put('buildScanPlugin.gradle-enterprise.url', geUrl)

        if (geExtensionVersion)
            configParameters.put('buildScanPlugin.gradle-enterprise.extension.version', geExtensionVersion)

        if (ccudExtensionVersion)
            configParameters.put('buildScanPlugin.ccud.extension.version', ccudExtensionVersion)

        if (geExtensionCustomCoordinates)
            configParameters.put('buildScanPlugin.gradle-enterprise.extension.custom.coordinates', geExtensionCustomCoordinates)

        if (ccudExtensionCustomCoordinates)
            configParameters.put('buildScanPlugin.ccud.extension.custom.coordinates', ccudExtensionCustomCoordinates)

        if (allowUntrustedServer != null)
            configParameters.put('buildScanPlugin.gradle-enterprise.allow-untrusted-server', allowUntrustedServer.toString())

        if (commandLineBuildStepEnabled != null)
            configParameters.put('buildScanPlugin.command-line-build-step.enabled', commandLineBuildStepEnabled.toString())
    }
}
