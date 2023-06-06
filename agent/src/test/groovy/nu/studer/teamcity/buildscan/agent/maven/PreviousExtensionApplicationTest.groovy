package nu.studer.teamcity.buildscan.agent.maven

import nu.studer.teamcity.buildscan.agent.TcPluginConfig
import nu.studer.teamcity.buildscan.agent.maven.testutils.MavenProject

import static org.junit.Assume.assumeTrue

class PreviousExtensionApplicationTest extends BaseExtensionApplicationTest {
    static final String GE_EXTENSION_VERSION_ONE_PREVIOUS = '1.16.6'
    static final String GE_EXTENSION_VERSION_TWO_PREVIOUS = '1.15.5'

    def "applies previous versions GE extension via classpath when not defined in project (#jdkCompatibleMavenVersion, GE Maven Extension #geExtensionVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        def mvnProject = new MavenProject.Configuration().buildIn(checkoutDir)

        and:
        def gePluginConfig = new TcPluginConfig(
                geUrl: GE_URL,
                geExtensionVersion: geExtensionVersion,
        )

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, mvnProject, gePluginConfig)

        then:
        1 * extensionApplicationListener.geExtensionApplied(geExtensionVersion)
        0 * extensionApplicationListener.ccudExtensionApplied(_)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        [jdkCompatibleMavenVersion , geExtensionVersion] << [SUPPORTED_MAVEN_VERSIONS, [TWO_PREVIOUS_GE_EXTENSION_VERSION, GE_EXTENSION_VERSION_ONE_PREVIOUS]].combinations()
    }
}
