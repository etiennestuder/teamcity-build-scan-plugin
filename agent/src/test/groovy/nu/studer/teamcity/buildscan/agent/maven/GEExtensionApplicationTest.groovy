package nu.studer.teamcity.buildscan.agent.maven

import nu.studer.teamcity.buildscan.agent.TcPluginConfig
import nu.studer.teamcity.buildscan.agent.maven.testutils.MavenBuildStepConfig
import nu.studer.teamcity.buildscan.agent.maven.testutils.MavenProject

import static org.junit.Assume.assumeTrue

class GEExtensionApplicationTest extends BaseExtensionApplicationTest {

    def "does not apply Develocity / CCUD extensions when not defined in project and not requested via TC config (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue DEVELOCITY_URL != null

        given:
        def mvnProject = new MavenProject.Configuration().buildIn(checkoutDir)

        and:
        def develocityPluginConfig = new TcPluginConfig()

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, mvnProject, develocityPluginConfig)

        then:
        0 * extensionApplicationListener.develocityExtensionApplied(_)
        0 * extensionApplicationListener.ccudExtensionApplied(_)

        and:
        outputMissesTeamCityServiceMessageBuildStarted(output)
        outputMissesTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "applies GE extension via classpath when not defined in project (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue DEVELOCITY_URL != null

        given:
        def mvnProject = new MavenProject.Configuration().buildIn(checkoutDir)

        and:
        def develocityPluginConfig = new TcPluginConfig(
            develocityUrl: DEVELOCITY_URL,
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

    def "applies GE extension via project when defined in project (#jdkCompatibleMavenVersion)"() {
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

    def "applies GE extension via classpath when not defined in project where pom location set (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue DEVELOCITY_URL != null

        given:
        def mvnProject = new MavenProject.Configuration().buildIn(checkoutDir)

        and:
        def develocityPluginConfig = new TcPluginConfig(
            develocityUrl: DEVELOCITY_URL,
            develocityExtensionVersion: DEVELOCITY_EXTENSION_VERSION,
        )

        and:
        def mvnBuildStepConfig = new MavenBuildStepConfig(
            checkoutDir: checkoutDir,
            pathToPomFile: getRelativePath(checkoutDir, mvnProject.pom),
        )
        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, mvnProject, develocityPluginConfig, mvnBuildStepConfig)

        then:
        1 * extensionApplicationListener.develocityExtensionApplied(DEVELOCITY_EXTENSION_VERSION)
        0 * extensionApplicationListener.ccudExtensionApplied(_)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "applies GE extension via project when defined in project where pom location set (#jdkCompatibleMavenVersion)"() {
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
        )

        and:
        def mvnBuildStepConfig = new MavenBuildStepConfig(
            checkoutDir: checkoutDir,
            pathToPomFile: getRelativePath(checkoutDir, mvnProject.pom),
        )

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, mvnProject, develocityPluginConfig, mvnBuildStepConfig)

        then:
        0 * extensionApplicationListener.develocityExtensionApplied(_)
        0 * extensionApplicationListener.ccudExtensionApplied(_)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "applies GE extension via classpath when not defined in project where checkout dir and working dir not set (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue DEVELOCITY_URL != null

        given:
        def mvnProject = new MavenProject.Configuration().buildIn(checkoutDir)

        and:
        def develocityPluginConfig = new TcPluginConfig(
            develocityUrl: DEVELOCITY_URL,
            develocityExtensionVersion: DEVELOCITY_EXTENSION_VERSION,
        )

        and:
        def mvnBuildStepConfig = new MavenBuildStepConfig(
            checkoutDir: null
        )

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, mvnProject, develocityPluginConfig, mvnBuildStepConfig)

        then:
        1 * extensionApplicationListener.develocityExtensionApplied(DEVELOCITY_EXTENSION_VERSION)
        0 * extensionApplicationListener.ccudExtensionApplied(_)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

}
