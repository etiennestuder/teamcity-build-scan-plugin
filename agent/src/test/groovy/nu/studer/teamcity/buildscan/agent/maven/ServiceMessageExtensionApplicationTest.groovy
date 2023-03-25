package nu.studer.teamcity.buildscan.agent.maven

import nu.studer.teamcity.buildscan.agent.TcPluginConfig
import nu.studer.teamcity.buildscan.agent.maven.testutils.MavenProject

import static org.junit.Assume.assumeTrue

class ServiceMessageExtensionApplicationTest extends BaseExtensionApplicationTest {

    def "build succeeds when service message maven extension is applied to a project without GE in the extension classpath (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        def mvnProject = new MavenProject.Configuration().buildIn(checkoutDir)

        and:
        def gePluginConfig = new TcPluginConfig(
            enableCommandLineRunner: true,
        )

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, mvnProject, gePluginConfig)

        then:
        outputContainsBuildSuccess(output)
        outputMissesTeamCityServiceMessageBuildStarted(output)
        outputMissesTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }
}
