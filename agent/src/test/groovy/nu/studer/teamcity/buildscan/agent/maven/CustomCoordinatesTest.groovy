package nu.studer.teamcity.buildscan.agent.maven

import nu.studer.teamcity.buildscan.agent.TcPluginConfig
import nu.studer.teamcity.buildscan.agent.maven.testutils.GroupArtifactVersion
import nu.studer.teamcity.buildscan.agent.maven.testutils.MavenProject

import static org.junit.Assume.assumeTrue

class CustomCoordinatesTest extends BaseExtensionApplicationTest {

    def "does not inject GE extension when not defined in project but matching custom coordinates defined in project (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        def mvnProject = new MavenProject.Configuration(
            geUrl: GE_URL,
            // using Guava as surrogate since we do not have a custom extension at hand that pulls in the GE Maven extension transitively
            customExtension: new GroupArtifactVersion(group: 'com.google.guava', artifact: 'guava', version: '31.1-jre')
        ).buildIn(checkoutDir)

        and:
        def gePluginConfig = new TcPluginConfig(
            geUrl: GE_URL,
            geExtensionVersion: GE_EXTENSION_VERSION,
            geExtensionCustomCoordinates: 'com.google.guava:guava',
        )

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, mvnProject, gePluginConfig)

        then:
        0 * extensionApplicationListener.geExtensionApplied(_)
        0 * extensionApplicationListener.ccudExtensionApplied(_)

        and:
        outputMissesTeamCityServiceMessageBuildStarted(output)
        outputMissesTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "does not inject CCUD extension when not defined in project but matching custom coordinates defined in project (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        def mvnProject = new MavenProject.Configuration(
            geUrl: GE_URL,
            geExtensionVersion: GE_EXTENSION_VERSION,
            // using Guava as surrogate since we do not have a custom extension at hand that pulls in the GE Maven extension transitively
            customExtension: new GroupArtifactVersion(group: 'com.google.guava', artifact: 'guava', version: '31.1-jre')
        ).buildIn(checkoutDir)

        and:
        def gePluginConfig = new TcPluginConfig(
            geUrl: GE_URL,
            ccudExtensionVersion: CCUD_EXTENSION_VERSION,
            ccudExtensionCustomCoordinates: 'com.google.guava:guava',
        )

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, mvnProject, gePluginConfig)

        then:
        0 * extensionApplicationListener.geExtensionApplied(_)
        0 * extensionApplicationListener.ccudExtensionApplied(_)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }
}
