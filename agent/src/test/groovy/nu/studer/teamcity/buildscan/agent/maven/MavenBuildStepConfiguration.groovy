package nu.studer.teamcity.buildscan.agent.maven

final class MavenBuildStepConfiguration {
    String goals = 'clean package'
    String pathToPomFile = null

    Map<String, String> applyTo(Map<String, String> configParameters, Map<String, String> runnerParameters) {
        applyRunnerParameters(runnerParameters)

        return runnerParameters
    }

    private void applyRunnerParameters(Map<String, String> runnerParameters) {
        runnerParameters.put('goals', goals)

        if (pathToPomFile)
            runnerParameters.put('pomLocation', pathToPomFile)
    }
}
