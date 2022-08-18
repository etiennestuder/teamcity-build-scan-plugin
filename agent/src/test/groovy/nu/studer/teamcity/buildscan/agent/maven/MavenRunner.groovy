package nu.studer.teamcity.buildscan.agent.maven

final class MavenRunner {

    File installationDir
    File projectDir
    File multiModuleProjectDir
    List<String> arguments

    String runBuild() {
        def installationBinDir = new File(installationDir, 'bin')
        def mvnExecutableName = System.getProperty('os.name').toLowerCase().contains('win') ? 'mvn.cmd' : 'mvn'
        def mvn = new File(installationBinDir, mvnExecutableName)
        def defaultArgs = ['-B', "-Dmaven.multiModuleProjectDirectory=${multiModuleProjectDir}".toString()]
        def userArgs = arguments.collectMany { s -> s.trim().split(' ').toList() }
        def command = [mvn.absolutePath] + defaultArgs + userArgs
        new ProcessBuilder(command)
            .directory(projectDir)
            .start()
            .text
    }

}
