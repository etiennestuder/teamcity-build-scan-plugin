package nu.studer.teamcity.buildscan.agent.maven

final class MavenBuildStepConfig {

    File checkoutDir
    String pathToPomFile
    String goals = 'clean package'
    boolean isVirtualContext = false

    Map<String, String> toRunnerParameters() {
        Map<String, String> runnerParams = [:]
        if (checkoutDir) {
            runnerParams.put('teamcity.build.checkoutDir', checkoutDir.absolutePath)
            runnerParams.put('teamcity.build.workingDir', checkoutDir.absolutePath)
        }
        if (pathToPomFile) {
            runnerParams.put('pomLocation', pathToPomFile)
        }
        if (goals) {
            runnerParams.put('goals', goals)
        }
        return runnerParams
    }

}
