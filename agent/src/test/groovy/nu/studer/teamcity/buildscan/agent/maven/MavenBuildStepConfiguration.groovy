package nu.studer.teamcity.buildscan.agent.maven

final class MavenBuildStepConfiguration {
    File checkoutDir = null
    String goals = 'clean package'
    String pathToPomFile = null

    Map<String, String> toRunnerParameters() {
        Map<String, String> runnerParameters = [:]

        runnerParameters.put('goals', goals)

        if (pathToPomFile)
            runnerParameters.put('pomLocation', pathToPomFile)

        if (checkoutDir) {
            runnerParameters.put('teamcity.build.checkoutDir', checkoutDir.absolutePath)
            runnerParameters.put('teamcity.build.workingDir', checkoutDir.absolutePath)
        }

        return runnerParameters
    }
}
