package nu.studer.teamcity.buildscan.agent.maven

import nu.studer.teamcity.buildscan.agent.TcPluginConfig

import static org.junit.Assume.assumeTrue

class CCUDExtensionApplicationTest extends BaseExtensionApplicationTest {

    def "applies CCUD extension via classpath when not defined in project where GE extension not defined in project and not applied via classpath (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        def mvnProject = new MavenProject.Configuration().buildIn(checkoutDir)

        and:
        def gePluginConfig = new TcPluginConfig(
            geUrl: GE_URL,
            ccudExtensionVersion: CCUD_EXTENSION_VERSION,
        )

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, mvnProject, gePluginConfig)

        then:
        0 * extensionApplicationListener.geExtensionApplied(_)
        1 * extensionApplicationListener.ccudExtensionApplied(CCUD_EXTENSION_VERSION)

        and:
        outputMissesTeamCityServiceMessageBuildStarted(output)
        outputMissesTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "applies CCUD extension via classpath when not defined in project where GE extension applied via classpath (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        def mvnProject = new MavenProject.Configuration().buildIn(checkoutDir)

        and:
        def gePluginConfig = new TcPluginConfig(
            geUrl: GE_URL,
            geExtensionVersion: GE_EXTENSION_VERSION,
            ccudExtensionVersion: CCUD_EXTENSION_VERSION,
        )

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, mvnProject, gePluginConfig)

        then:
        1 * extensionApplicationListener.geExtensionApplied(GE_EXTENSION_VERSION)
        1 * extensionApplicationListener.ccudExtensionApplied(CCUD_EXTENSION_VERSION)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "applies CCUD extension via classpath when not defined in project where GE extension defined in project (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        def mvnProject = new MavenProject.Configuration(
            geUrl: GE_URL,
            geExtensionVersion: GE_EXTENSION_VERSION,
        ).buildIn(checkoutDir)

        and:
        def gePluginConfig = new TcPluginConfig(
            geUrl: GE_URL,
            geExtensionVersion: GE_EXTENSION_VERSION,
            ccudExtensionVersion: CCUD_EXTENSION_VERSION,
        )

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, mvnProject, gePluginConfig)

        then:
        0 * extensionApplicationListener.geExtensionApplied(_)
        1 * extensionApplicationListener.ccudExtensionApplied(CCUD_EXTENSION_VERSION)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "applies CCUD extension via project when defined in project (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        def mvnProject = new MavenProject.Configuration(
            geUrl: GE_URL,
            geExtensionVersion: GE_EXTENSION_VERSION,
            ccudExtensionVersion: CCUD_EXTENSION_VERSION
        ).buildIn(checkoutDir)

        and:
        def gePluginConfig = new TcPluginConfig(
            geUrl: GE_URL,
            geExtensionVersion: GE_EXTENSION_VERSION,
            ccudExtensionVersion: CCUD_EXTENSION_VERSION,
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
