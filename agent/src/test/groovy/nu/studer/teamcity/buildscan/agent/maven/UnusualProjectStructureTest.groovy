package nu.studer.teamcity.buildscan.agent.maven

import nu.studer.teamcity.buildscan.agent.TcPluginConfig

import static org.junit.Assume.assumeTrue

class UnusualProjectStructureTest extends BaseExtensionApplicationTest {

    def "publishes build scan when pom is in a subdirectory and extensions.xml is in project root directory (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        def mvnProject = new MavenProject.Configuration(
            geUrl: GE_URL,
            geExtensionVersion: GE_EXTENSION_VERSION,
            pomDirName: 'subdir'
        ).buildIn(checkoutDir)

        and:
        def gePluginConfig = new TcPluginConfig(
            geExtensionVersion: GE_EXTENSION_VERSION,
        )

        and:
        def mvnBuildStepConfig = new MavenBuildStepConfig(
            checkoutDir: checkoutDir,
            pathToPomFile: getRelativePath(checkoutDir, mvnProject.pom)
        )

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, mvnProject, gePluginConfig, mvnBuildStepConfig)

        then:
        outputContainsBuildSuccess(output)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "publishes build scan when pom is in a subdirectory and subdirectory is specified as pom path and extensions.xml is in project root directory (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        def mvnProject = new MavenProject.Configuration(
            geUrl: GE_URL,
            geExtensionVersion: GE_EXTENSION_VERSION,
            pomDirName: 'subdir'
        ).buildIn(checkoutDir)

        and:
        def gePluginConfig = new TcPluginConfig(
            geExtensionVersion: GE_EXTENSION_VERSION,
        )

        and:
        def mvnBuildStepConfig = new MavenBuildStepConfig(
            checkoutDir: checkoutDir,
            pathToPomFile: getRelativePath(checkoutDir, mvnProject.pom.parentFile)
        )

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, mvnProject, gePluginConfig, mvnBuildStepConfig)

        then:
        outputContainsBuildSuccess(output)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

    def "publishes build scan when pom is in a subdirectory and extensions.xml is in a higher subdirectory (#jdkCompatibleMavenVersion)"() {
        assumeTrue jdkCompatibleMavenVersion.isJvmVersionCompatible()
        assumeTrue GE_URL != null

        given:
        def mvnProject = new MavenProject.Configuration(
            geUrl: GE_URL,
            geExtensionVersion: GE_EXTENSION_VERSION,
            pomDirName: 'subdir1/subdir2',
            dotMvnParentDirName: 'subdir1',
        ).buildIn(checkoutDir)

        and:
        def gePluginConfig = new TcPluginConfig(
            geExtensionVersion: GE_EXTENSION_VERSION,
        )

        and:
        def mvnBuildStepConfig = new MavenBuildStepConfig(
            checkoutDir: checkoutDir,
            pathToPomFile: getRelativePath(checkoutDir, mvnProject.pom)
        )

        when:
        def output = run(jdkCompatibleMavenVersion.mavenVersion, mvnProject, gePluginConfig, mvnBuildStepConfig)

        then:
        outputContainsBuildSuccess(output)

        and:
        outputContainsTeamCityServiceMessageBuildStarted(output)
        outputContainsTeamCityServiceMessageBuildScanUrl(output)

        where:
        jdkCompatibleMavenVersion << SUPPORTED_MAVEN_VERSIONS
    }

}
