package nu.studer.teamcity.buildscan.agent.maven

final class MavenRunner {

    String version
    File installationDir
    File projectDir
    File multiModuleProjectDir
    List<String> arguments

    String build() {
        checkForRequiredProperties()
        installMaven()
        run()
    }

    private void checkForRequiredProperties() {
        if (!version) {
            throw new IllegalStateException("Maven version is not set")
        }

        if (!installationDir) {
            throw new IllegalStateException("Maven installation directory is not set")
        }

        if (!projectDir) {
            throw new IllegalStateException("Maven project directory is not set")
        }

        if (!multiModuleProjectDir) {
            throw new IllegalStateException("multiModuleProjectDirectory is not set")
        }
    }

    private void installMaven() {
        ["mvn", "mvn.cmd"].each { mvn ->
            def file = new File(installationDir, mvn)
            file << getClass().getResourceAsStream("/$mvn")
            file.setExecutable(true)
        }

        def mavenWrapperProperties = new File(installationDir, '.mvn/wrapper/maven-wrapper.properties')
        mavenWrapperProperties.parentFile.mkdirs()
        mavenWrapperProperties << """distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$version/apache-maven-$version-bin.zip
wrapperUrl=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.1.0/maven-wrapper-3.1.0.jar
"""
    }

    private String run() {
        def mvnExecutableName = System.getProperty('os.name').startsWith('Windows') ? 'mvn.cmd' : 'mvn'
        def mvn = new File(installationDir, mvnExecutableName)

        // because we are running a maven wrapper from a different directory, we set the multiModuleProjectDirectory to where the .mvn folder is
        // this is how Maven finds the .mvn folder, but the maven wrapper only finds it relative to the mvn wrapper itself
        def defaultArgs = ['-B', "-Dmaven.multiModuleProjectDirectory=${multiModuleProjectDir}".toString()]
        def userArgs = arguments.collectMany { s -> s.trim().split(' ').toList() }
        def command = [mvn.absolutePath] + defaultArgs + userArgs

        new ProcessBuilder(command)
            .directory(projectDir)
            .start()
            .text
    }

}
