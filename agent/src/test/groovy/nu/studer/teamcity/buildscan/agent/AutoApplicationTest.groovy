package nu.studer.teamcity.buildscan.agent

import static org.junit.Assume.assumeTrue

class AutoApplicationTest extends BaseInitScriptTest {

    def "sends build scan url service message when auto applying Build Scan / Gradle Enterprise plugin (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        settingsFile << ''
        buildFile << ''

        when:
        def result = run(jdkCompatibleGradleVersion.gradleVersion, [
                "-DteamCityBuildScanPlugin.gradle-enterprise.url=$mockScansServer.address".toString(),
                '-DteamCityBuildScanPlugin.gradle-enterprise.plugin.version=3.10',
                '-DteamCityBuildScanPlugin.ccud.plugin.version=1.6.5',
        ])

        then:
        outputContainsTeamCityServiceMessageBuildStarted(result)
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << SUPPORTED_GRADLE_VERSIONS
    }

    def "sends build scan url service message when Build Scan / Gradle Enterprise plugin is configured and auto-applied (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        settingsFile << maybeAddGradleEnterprisePlugin(jdkCompatibleGradleVersion.gradleVersion)
        buildFile << maybeAddBuildScanPlugin(jdkCompatibleGradleVersion.gradleVersion)

        when:
        def result = run(jdkCompatibleGradleVersion.gradleVersion, [
                "-DteamCityBuildScanPlugin.gradle-enterprise.url=$mockScansServer.address".toString(),
                '-DteamCityBuildScanPlugin.gradle-enterprise.plugin.version=3.10',
                '-DteamCityBuildScanPlugin.ccud.plugin.version=1.6.5',
        ])

        then:
        outputContainsTeamCityServiceMessageBuildStarted(result)
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << SUPPORTED_GRADLE_VERSIONS
    }

    def "url system property overrides gradle enterprise configuration (#jdkCompatibleGradleVersion)"() {
        assumeTrue jdkCompatibleGradleVersion.isJvmVersionCompatible()

        given:
        settingsFile << maybeAddGradleEnterprisePlugin(jdkCompatibleGradleVersion.gradleVersion, 'https://ge-server.invalid/')
        buildFile << maybeAddBuildScanPlugin(jdkCompatibleGradleVersion.gradleVersion, 'https://ge-server.invalid/')

        when:
        def result = run(jdkCompatibleGradleVersion.gradleVersion, [
                "-DteamCityBuildScanPlugin.gradle-enterprise.url=$mockScansServer.address".toString(),
        ])

        then:
        outputContainsTeamCityServiceMessageBuildScanUrl(result)

        where:
        jdkCompatibleGradleVersion << SUPPORTED_GRADLE_VERSIONS
    }

}
