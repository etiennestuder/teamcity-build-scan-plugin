package nu.studer.teamcity.buildscan.agent.maven

import nu.studer.teamcity.buildscan.agent.TcPluginConfig
import nu.studer.teamcity.buildscan.agent.maven.testutils.MavenProject

import static org.junit.Assume.assumeTrue

class UrlConfigurationExtensionApplicationTest extends BaseExtensionApplicationTest {

    def "ignores Develocity URL requested via TC config when Develocity extension is not applied via the classpath (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue DEVELOCITY_URL != null

        given:
        def mvnProject = new MavenProject.Configuration(
            develocityUrl: DEVELOCITY_URL,
            develocityExtensionVersion: DEVELOCITY_EXTENSION_VERSION,
        ).buildIn(checkoutDir)

        and:
        def develocityPluginConfig = new TcPluginConfig(
            develocityUrl: new URI('https://dv-server.invalid'),
            develocityAllowUntrustedServer: true,
            develocityExtensionVersion: DEVELOCITY_EXTENSION_VERSION,
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

    def "configures Develocity URL requested via TC config when Develocity extension is applied via classpath (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue DEVELOCITY_URL != null

        given:
        def mvnProject = new MavenProject.Configuration(
            develocityUrl: 'https://dv-server.invalid',
            develocityExtensionVersion: null,
        ).buildIn(checkoutDir)

        and:
        def develocityPluginConfig = new TcPluginConfig(
            develocityUrl: DEVELOCITY_URL,
            develocityAllowUntrustedServer: true,
            develocityExtensionVersion: DEVELOCITY_EXTENSION_VERSION,
        )

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, mvnProject, develocityPluginConfig)

        then:
        1 * extensionApplicationListener.develocityExtensionApplied(DEVELOCITY_EXTENSION_VERSION)
        0 * extensionApplicationListener.ccudExtensionApplied(_)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "enforces Develocity URL and allowUntrustedServer in project if enforce url parameter is enabled (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue DEVELOCITY_URL != null

        given:
        def mvnProject = new MavenProject.Configuration(
            develocityUrl: new URI('https://dv-server.invalid'),
            develocityExtensionVersion: DEVELOCITY_EXTENSION_VERSION,
        ).buildIn(checkoutDir)

        and:
        def develocityPluginConfig = new TcPluginConfig(
            develocityUrl: DEVELOCITY_URL,
            develocityAllowUntrustedServer: true,
            develocityEnforceUrl: true,
            develocityExtensionVersion: DEVELOCITY_EXTENSION_VERSION,
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
