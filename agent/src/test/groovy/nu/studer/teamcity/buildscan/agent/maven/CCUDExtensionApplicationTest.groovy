package nu.studer.teamcity.buildscan.agent.maven

import nu.studer.teamcity.buildscan.agent.TcPluginConfig
import nu.studer.teamcity.buildscan.agent.maven.testutils.MavenProject

import static org.junit.Assume.assumeTrue

class CCUDExtensionApplicationTest extends BaseExtensionApplicationTest {

    def "applies CCUD extension via classpath when not defined in project where Develocity extension not defined in project and not applied via classpath (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue DEVELOCITY_URL != null

        given:
        def mvnProject = new MavenProject.Configuration().buildIn(checkoutDir)

        and:
        def develocityPluginConfig = new TcPluginConfig(
            develocityUrl: DEVELOCITY_URL,
            ccudExtensionVersion: CCUD_EXTENSION_VERSION,
        )

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, mvnProject, develocityPluginConfig)

        then:
        0 * extensionApplicationListener.develocityExtensionApplied(_)
        1 * extensionApplicationListener.ccudExtensionApplied(CCUD_EXTENSION_VERSION)

        and:
        outputMissesTeamCityServiceMessageBuildStarted(output)
        outputMissesTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "applies CCUD extension via classpath when not defined in project where Develocity extension applied via classpath (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue DEVELOCITY_URL != null

        given:
        def mvnProject = new MavenProject.Configuration().buildIn(checkoutDir)

        and:
        def develocityPluginConfig = new TcPluginConfig(
            develocityUrl: DEVELOCITY_URL,
            develocityExtensionVersion: DEVELOCITY_EXTENSION_VERSION,
            ccudExtensionVersion: CCUD_EXTENSION_VERSION,
        )

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, mvnProject, develocityPluginConfig)

        then:
        1 * extensionApplicationListener.develocityExtensionApplied(DEVELOCITY_EXTENSION_VERSION)
        1 * extensionApplicationListener.ccudExtensionApplied(CCUD_EXTENSION_VERSION)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "applies CCUD extension via classpath when not defined in project where Develocity extension defined in project (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue DEVELOCITY_URL != null

        given:
        def mvnProject = new MavenProject.Configuration(
            develocityUrl: DEVELOCITY_URL,
            develocityExtensionVersion: DEVELOCITY_EXTENSION_VERSION,
        ).buildIn(checkoutDir)

        and:
        def develocityPluginConfig = new TcPluginConfig(
            develocityUrl: DEVELOCITY_URL,
            develocityExtensionVersion: DEVELOCITY_EXTENSION_VERSION,
            ccudExtensionVersion: CCUD_EXTENSION_VERSION,
        )

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, mvnProject, develocityPluginConfig)

        then:
        0 * extensionApplicationListener.develocityExtensionApplied(_)
        1 * extensionApplicationListener.ccudExtensionApplied(CCUD_EXTENSION_VERSION)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "applies CCUD extension via project when defined in project (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue DEVELOCITY_URL != null

        given:
        def mvnProject = new MavenProject.Configuration(
            develocityUrl: DEVELOCITY_URL,
            develocityExtensionVersion: DEVELOCITY_EXTENSION_VERSION,
            ccudExtensionVersion: CCUD_EXTENSION_VERSION
        ).buildIn(checkoutDir)

        and:
        def develocityPluginConfig = new TcPluginConfig(
            develocityUrl: DEVELOCITY_URL,
            develocityExtensionVersion: DEVELOCITY_EXTENSION_VERSION,
            ccudExtensionVersion: CCUD_EXTENSION_VERSION,
        )

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, mvnProject, develocityPluginConfig)

        then:
        0 * extensionApplicationListener.develocityExtensionApplied(_)
        0 * extensionApplicationListener.ccudExtensionApplied(_)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }
}
