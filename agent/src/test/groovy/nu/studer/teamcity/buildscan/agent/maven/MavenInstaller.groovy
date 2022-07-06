package nu.studer.teamcity.buildscan.agent.maven

final class MavenInstaller {

    String version
    File installationDir

    void installMaven() {
        def installationBinDir = new File(installationDir, "bin")
        installationBinDir.mkdirs()

        ["mvn", "mvn.cmd"].each { mvn ->
            def file = new File(installationBinDir, mvn)
            file << getClass().getResourceAsStream("/$mvn")
            file.setExecutable(true)
        }

        def mavenWrapperProperties = new File(installationBinDir, '.mvn/wrapper/maven-wrapper.properties')
        mavenWrapperProperties.parentFile.mkdirs()
        mavenWrapperProperties << """distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$version/apache-maven-$version-bin.zip
wrapperUrl=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.1.0/maven-wrapper-3.1.0.jar
"""
    }
}
